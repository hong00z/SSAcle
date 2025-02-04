package com.example.firstproject.ui.ai

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.example.firstproject.BuildConfig
import com.example.firstproject.MainActivity
import com.example.firstproject.databinding.FragmentAiBinding
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

class AiFragment : Fragment() {

    private var _binding: FragmentAiBinding? = null
    private val binding get() = _binding!!

    // API 키 및 전역 변수
    private var openAIApiKey = BuildConfig.OPENAI_API_KEY
    private var pdfContent: String = ""         // 원본 PDF 텍스트
    private var revisedHtmlContent: String = ""   // ChatGPT가 반환한 HTML

    // PDF 선택을 위한 Activity Result Launcher
    private val pdfPickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.tvPdfPath.text = it.path ?: "알 수 없는 경로"
                val parsedText = parsePdfToText(it)
                if (parsedText.isNotEmpty()) {
                    pdfContent = parsedText
                    Toast.makeText(requireContext(), "PDF 내용이 성공적으로 로드되었습니다.", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "PDF 내용을 불러오지 못했습니다.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // PDFBox 초기화 (Fragment 내에서는 requireContext() 사용)
        PDFBoxResourceLoader.init(requireContext())
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        _binding = FragmentAiBinding.inflate(inflater, container, false)

        binding.apply {
            backButton.setOnClickListener {
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }


            // WebView 기본 설정 (실시간 HTML 미리보기 용도)
            webView.settings.javaScriptEnabled = true
            webView.settings.defaultTextEncodingName = "UTF-8"


            // 1) PDF 파일 선택
            btnSelectPdf.setOnClickListener {
                selectPdfFile()
            }

            // 2) ChatGPT에 수정 요청 (실시간 스트리밍 방식)
            btnSubmit.setOnClickListener {
                if (pdfContent.isEmpty()) {
                    Toast.makeText(requireContext(), "먼저 PDF 파일을 선택하세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val userPrompt = editTextPrompt.text.toString().trim()
                // 기존 최종 결과 초기화
                revisedHtmlContent = ""
                tvResult.text = "수정 중..."
                callChatGPTToRevisePdfStreaming(pdfContent, userPrompt)
            }

            // 3) iText를 이용하여 수정된 HTML → PDF 변환 & MediaStore 저장
            btnDownloadPdf.setOnClickListener {
                if (revisedHtmlContent.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "먼저 ChatGPT 교정 결과를 받아야 합니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                savePdfUsingIText(revisedHtmlContent) { savedUri ->
                    if (savedUri != null) {
                        Toast.makeText(requireContext(), "다운로드가 완료되었습니다.", Toast.LENGTH_SHORT)
                            .show()
                        showDownloadCompleteNotification()
                        sharePdfToKakao(savedUri)
                    } else {
                        Toast.makeText(requireContext(), "PDF 변환/다운로드에 실패했습니다.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
        return binding.root
    }


    /**
     * PDF 파일 선택
     */
    private fun selectPdfFile() {
        // MIME 타입 "application/pdf"로 파일 선택
        pdfPickerLauncher.launch("application/pdf")
    }

    /**
     * PDF -> 텍스트 추출 (PDFBox 사용)
     */
    private fun parsePdfToText(pdfUri: Uri): String {
        return try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(pdfUri)
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
        // (1) 사용자 프롬프트가 비어있을 경우 기본 프롬프트 사용
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

        // (3) 시스템 메시지 구성
        val systemMessage = JSONObject().apply {
            put("role", "system")
            put(
                "content", """
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
            """.trimIndent()
            )
        }

        // (4) 사용자 메시지 구성
        val userMessage = JSONObject().apply {
            put("role", "user")
            put(
                "content", """
                [PDF Content Start]
                $pdfText
                [PDF Content End]
                
                [User Prompt]
                $finalPrompt
            """.trimIndent()
            )
        }

        val messagesArray = JSONArray().apply {
            put(systemMessage)
            put(userMessage)
        }

        // (5) 요청 본문 구성 (스트리밍 옵션 포함)
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

        // (6) 스트리밍 방식으로 비동기 호출 (코루틴 사용)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errorMsg = "Error: ${response.code}"
                        requireActivity().runOnUiThread {
                            binding.tvResult.text = errorMsg
                        }
                        return@launch
                    }

                    val source: BufferedSource? = response.body?.source()
                    if (source == null) {
                        requireActivity().runOnUiThread {
                            binding.tvResult.text = "응답을 읽을 수 없습니다."
                        }
                        return@launch
                    }

                    // 스트림으로 한 줄씩 읽으며 파싱
                    val reader = BufferedReader(InputStreamReader(source.inputStream(), "UTF-8"))
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
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
                                        // 실시간으로 WebView 업데이트
                                        requireActivity().runOnUiThread {
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
                                            binding.webView.loadDataWithBaseURL(
                                                null,
                                                previewHtml,
                                                "text/html",
                                                "UTF-8",
                                                null
                                            )
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    requireActivity().runOnUiThread {
                        binding.tvResult.text = "교정 완료! 아래는 미리보기에요."
                        binding.btnSubmit.text = "피드백 다시 받기"
                        binding.btnDownloadPdf.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    binding.tvResult.text = "네트워크 오류가 발생했습니다."
                }
            }
        }
    }

    /**
     * iText(html2pdf)를 사용하여 revisedHtmlContent를 PDF로 변환 후 MediaStore에 저장
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun savePdfUsingIText(htmlString: String, onSaved: (Uri?) -> Unit) {
        try {
            // 1) ConverterProperties 설정
            val converterProperties = ConverterProperties()
            val fontProvider = DefaultFontProvider(true, true, true)
            // 필요 시 사용자 폰트 추가 (예: assets 내 폰트)
            fontProvider.addFont("assets/NotoSansKR-Regular.ttf")
            converterProperties.setFontProvider(fontProvider)

            // 2) HTML을 PDF 바이트 배열로 변환
            val pdfBytes = ByteArrayOutputStream().use { bos ->
                HtmlConverter.convertToPdf(htmlString, bos, converterProperties)
                bos.toByteArray()
            }

            // 3) MediaStore.Downloads에 저장
            val resolver = requireContext().contentResolver
            val downloadsUri =
                MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
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
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("수정 완료")
            .setContentText("PDF가 기기에 저장되었습니다.")
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }

    /**
     * PDF를 카카오톡으로 공유
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
            Toast.makeText(requireContext(), "카카오톡으로 공유할 수 없습니다 (앱 미설치 등).", Toast.LENGTH_LONG)
                .show()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    // 콘텐츠 높이가 제대로 계산되도록 약간의 딜레이를 줄 수도 있습니다.
                    webView.post {
                        val contentHeight = (webView.contentHeight * webView.scale).toInt()
                        val maxHeight = (350 * webView.resources.displayMetrics.density).toInt()
                        val newHeight = if (contentHeight > maxHeight) maxHeight else contentHeight

                        // 레이아웃 파라미터를 업데이트 합니다.
                        webView.layoutParams.height = newHeight
                        webView.requestLayout()
                    }
                }

            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): AiFragment {
            return AiFragment()
        }
    }
}