package com.example.openaiprompt

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.itextpdf.html2pdf.ConverterProperties
import com.itextpdf.html2pdf.HtmlConverter
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSource
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var btnSelectPdf: Button
    private lateinit var tvPdfPath: TextView
    private lateinit var editTextPrompt: EditText
    private lateinit var btnSubmit: Button
    private lateinit var tvResult: TextView
    private lateinit var webView: WebView
    private lateinit var btnDownloadPdf: Button

    private var openAIApiKey = BuildConfig.OPENAI_API_KEY
    private var pdfContent: String = ""         // 원본 PDF 텍스트
    private var revisedHtmlContent: String = "" // ChatGPT가 반환한 HTML

    companion object {
        private const val REQUEST_CODE_SELECT_PDF = 100
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(permission), 1001)
            }
        }

        // PDFBox 초기화
        PDFBoxResourceLoader.init(applicationContext)

        btnSelectPdf = findViewById(R.id.btnSelectPdf)
        tvPdfPath = findViewById(R.id.tvPdfPath)
        editTextPrompt = findViewById(R.id.editTextPrompt)
        btnSubmit = findViewById(R.id.btnSubmit)
        tvResult = findViewById(R.id.tvResult)
        webView = findViewById(R.id.webView)
        btnDownloadPdf = findViewById(R.id.btnDownloadPdf)

        // WebView 기본 설정 (실시간 HTML 미리보기 용도)
        webView.settings.javaScriptEnabled = true
        webView.settings.defaultTextEncodingName = "UTF-8"
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }

        // 1) PDF 선택
        btnSelectPdf.setOnClickListener {
            selectPdfFile()
        }

        // 2) ChatGPT에 수정 요청 (실시간 스트리밍 방식)
        btnSubmit.setOnClickListener {
            if (pdfContent.isEmpty()) {
                Toast.makeText(this, "먼저 PDF 파일을 선택하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val userPrompt = editTextPrompt.text.toString().trim()
            // 기존의 최종 결과 초기화
            revisedHtmlContent = ""
            tvResult.text = "수정 중..."
            callChatGPTToRevisePdfStreaming(pdfContent, userPrompt)
        }

        // 3) iText로 수정된 HTML → PDF 변환 & MediaStore 저장
        btnDownloadPdf.setOnClickListener {
            if (revisedHtmlContent.isEmpty()) {
                Toast.makeText(this, "먼저 ChatGPT 교정 결과를 받아야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            savePdfUsingIText(revisedHtmlContent) { savedUri ->
                if (savedUri != null) {
                    Toast.makeText(this, "다운로드가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    showDownloadCompleteNotification()
                    sharePdfToKakao(savedUri)
                } else {
                    Toast.makeText(this, "PDF 변환/다운로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * PDF 파일 선택 (ACTION_GET_CONTENT)
     */
    private fun selectPdfFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, REQUEST_CODE_SELECT_PDF)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 알림 권한 허용됨
            } else {
                // 알림 권한 거부됨
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_PDF && resultCode == RESULT_OK) {
            val pdfUri: Uri? = data?.data
            if (pdfUri != null) {
                tvPdfPath.text = pdfUri.path ?: "알 수 없는 경로"
                val parsedText = parsePdfToText(pdfUri)
                if (parsedText.isNotEmpty()) {
                    pdfContent = parsedText
                    Toast.makeText(this, "PDF 내용이 성공적으로 로드되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "PDF 내용을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * PDF -> 텍스트 추출 (PDFBox)
     */
    private fun parsePdfToText(pdfUri: Uri): String {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(pdfUri)
            val document: PDDocument = PDDocument.load(inputStream)
            val stripper = PDFTextStripper()
            val text = stripper.getText(document)
            document.close()
            text
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * ChatGPT API 호출 (스트리밍 방식) → 실시간으로 WebView 업데이트
     */
    private fun callChatGPTToRevisePdfStreaming(pdfText: String, userPrompt: String) {
        // (1) 사용자 프롬프트가 비어있을 때 기본 프롬프트
        val defaultPrompt = """
            Please proofread and improve the text while considering both grammatical accuracy and HR relevance.
            - Whenever you change or revise any part of the original text, make that revised portion appear in <b style="color:red"> ... </b>.
            - Use <p> tags for paragraph separation.
            - If the content includes self-introduction questions and user answers, clearly separate the question part from the answer part.
            - Additionally, if a sentence ends with “바랍니다.”, treat it as a question and insert a blank line before the following answer. For example:
                <p>...바랍니다.</p>
                <p></p>
                <p>The next sentence (the answer)</p>
            - The final output must be in Korean only.
            - Output valid HTML only, with no extra commentary.
        """.trimIndent()

        val finalPrompt = if (userPrompt.isEmpty()) defaultPrompt else userPrompt

        // (2) OkHttp 클라이언트 설정
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val url = "https://api.openai.com/v1/chat/completions"

        // (3) 시스템 메시지
        val systemMessage = JSONObject().apply {
            put("role", "system")
            put("content", """
                You are an advanced proofreading system and a senior HR manager with 10 years of experience in hiring developers.
                The user will provide PDF content and an additional prompt.
                Your job is to provide corrected or improved text while considering both grammatical accuracy and HR relevance.
                If no change is needed, leave it as is.
                
                Important:
                - Wrap changed words/phrases in <b> tags
                - Whenever you change or revise any part of the original text, make that revised portion appear in red (e.g., <b style="color:red"> ... </b>).
                - Use <p> tags for paragraph separation.
                - If the content includes self-introduction questions and user answers, clearly separate the question part from the answer part.
                - Additionally, if a sentence ends with “바랍니다.”, treat it as a question and insert a blank line before the following answer. For example:
                  <p>...바랍니다.</p>
                  <p></p>
                  <p>The next sentence (the answer)</p>
                - The final output must be in Korean only.
                - Output valid HTML only, with no extra commentary.
            """.trimIndent())
        }

        // (4) 사용자 메시지
        val userMessage = JSONObject().apply {
            put("role", "user")
            put("content", """
                [PDF Content Start]
                $pdfText
                [PDF Content End]
                
                [User Prompt]
                $finalPrompt
            """.trimIndent())
        }

        val messagesArray = JSONArray().apply {
            put(systemMessage)
            put(userMessage)
        }

        // (5) 요청 본문 구성 (스트리밍 옵션 추가)
        val jsonBody = JSONObject().apply {
            put("model", "gpt-4o")
            put("messages", messagesArray)
            put("stream", true)
        }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = RequestBody.create(mediaType, jsonBody.toString())

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $openAIApiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        // (6) 스트리밍 방식으로 비동기 호출
        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errorMsg = "Error: ${response.code}"
                        runOnUiThread { tvResult.text = errorMsg }
                        return@launch
                    }

                    val source: BufferedSource? = response.body?.source()
                    if (source == null) {
                        runOnUiThread { tvResult.text = "응답을 읽을 수 없습니다." }
                        return@launch
                    }

                    // 스트림으로 한 줄씩 읽으며 파싱
                    val reader = BufferedReader(InputStreamReader(source.inputStream(), "UTF-8"))
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        // OpenAI 스트리밍 응답은 "data:" 로 시작함
                        if (line!!.startsWith("data:")) {
                            val data = line!!.removePrefix("data:").trim()
                            if (data == "[DONE]") {
                                break
                            }
                            try {
                                val jsonData = JSONObject(data)
                                val choices = jsonData.getJSONArray("choices")
                                if (choices.length() > 0) {
                                    val delta = choices.getJSONObject(0).getJSONObject("delta")
                                    val contentChunk = delta.optString("content", "")
                                    if (contentChunk.isNotEmpty()) {
                                        revisedHtmlContent += contentChunk
                                        // 실시간으로 WecbView 업데이트
                                        runOnUiThread {
                                            val previewHtml = """
                                                <html>
                                                <head>
                                                  <meta charset="UTF-8"/>
                                                  <style>
                                                    body {
                                                        font-size: 14px;
                                                        line-height: 1.4;
                                                        padding: 16px;
                                                        word-wrap: break-word;
                                                    }
                                                    b {
                                                        color: #d32f2f;
                                                    }
                                                  </style>
                                                </head>
                                                <body>
                                                    $revisedHtmlContent
                                                </body>
                                                </html>
                                            """.trimIndent()
                                            webView.loadDataWithBaseURL(null, previewHtml, "text/html", "UTF-8", null)
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    runOnUiThread {
                        tvResult.text = "교정 완료! 아래 WebView에서 최종 미리보기를 확인하세요."
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    tvResult.text = "네트워크 오류가 발생했습니다."
                }
            }
        }
    }

    /**
     * iText(html2pdf)로 revisedHtmlContent를 PDF로 변환하여 MediaStore에 저장
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun savePdfUsingIText(htmlString: String, onSaved: (Uri?) -> Unit) {
        try {
            // 1) ConverterProperties 설정
            val converterProperties = ConverterProperties()
            val fontProvider = DefaultFontProvider(true, true, true)
            // 필요 시 사용자 폰트 추가 (예: assets 폴더 내 폰트)
            fontProvider.addFont("assets/NotoSansKR-Regular.ttf")
            converterProperties.setFontProvider(fontProvider)

            // 2) HTML을 PDF 바이트 배열로 변환
            val pdfBytes = ByteArrayOutputStream().use { bos ->
                HtmlConverter.convertToPdf(htmlString, bos, converterProperties)
                bos.toByteArray()
            }

            // 3) MediaStore.Downloads에 저장
            val resolver = contentResolver
            val downloadsUri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val currentDate = dateFormat.format(Date())
            val fileName = "자소서수정_${currentDate}.pdf"

            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, "Download/")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val itemUri = resolver.insert(downloadsUri, contentValues)
            if (itemUri == null) {
                onSaved(null)
                return
            }

            resolver.openFileDescriptor(itemUri, "rw")?.use { pfd ->
                FileOutputStream(pfd.fileDescriptor).use { fos ->
                    fos.write(pdfBytes)
                    fos.flush()
                }
            }

            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(itemUri, contentValues, null, null)
            onSaved(itemUri)
        } catch (e: Exception) {
            e.printStackTrace()
            onSaved(null)
        }
    }

    /**
     * "다운로드 완료" 알림
     */
    private fun showDownloadCompleteNotification() {
        val channelId = "download_channel"
        val channelName = "PDF 다운로드 알림"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("수정 완료")
            .setContentText("PDF가 기기에 저장되었습니다.")
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }

    /**
     * PDF 공유 (카카오톡)
     */
    private fun sharePdfToKakao(pdfUri: Uri) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                setPackage("com.kakao.talk")
                putExtra(Intent.EXTRA_STREAM, pdfUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(shareIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "카카오톡으로 공유할 수 없습니다 (앱 미설치 등).", Toast.LENGTH_LONG).show()
        }
    }
}
