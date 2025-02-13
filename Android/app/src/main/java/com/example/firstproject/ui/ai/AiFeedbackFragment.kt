package com.example.firstproject.ui.ai

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.example.firstproject.BuildConfig
import com.example.firstproject.MainActivity
import com.example.firstproject.R
import com.example.firstproject.databinding.FragmentAiBinding
import com.example.firstproject.databinding.FragmentAiFeedbackBinding
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


class AiFeedbackFragment : Fragment() {


    private var _binding: FragmentAiFeedbackBinding? = null
    private val binding get() = _binding!!

    // API 키 및 전역 변수
    private var openAIApiKey = BuildConfig.OPENAI_API_KEY
    private var pdfContent: String = ""         // 원본 PDF 텍스트
    private var revisedHtmlContent: String = ""   // ChatGPT가 반환한 HTML
    private var isBtnSubmitClicked = false
    var isMarginIncreased = false   // margin이 증가했는지 여부

    // PDF 선택을 위한 Activity Result Launcher
    private val pdfPickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val parsedText = parsePdfToText(it)
                if (parsedText.isNotEmpty()) {
                    pdfContent = parsedText
                    Toast.makeText(requireContext(), "PDF 내용이 성공적으로 로드되었습니다.", Toast.LENGTH_SHORT)
                        .show()
                    binding.changeText.text = "업로드 되었습니다."

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
        _binding = FragmentAiFeedbackBinding.inflate(inflater, container, false)

        binding.apply {
            backButton.setOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
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

                binding.changeFeedback.text = "수정중..."

                // 기존 최종 결과 초기화
                revisedHtmlContent = ""
                webView.visibility = View.VISIBLE

                callChatGPTToRevisePdfStreaming(pdfContent)
                isBtnSubmitClicked = true  // btnSubmit 클릭 시 true로 설정

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


            binding.help.setOnClickListener {
                val overlay = binding.helpOverlay
                val layoutParams = overlay.layoutParams as ViewGroup.MarginLayoutParams

                if (overlay.visibility != View.VISIBLE) {
                    overlay.visibility = View.VISIBLE

                    if (isBtnSubmitClicked && !isMarginIncreased) {
                        layoutParams.topMargin += resources.getDimensionPixelSize(R.dimen.dp_35)
                        overlay.layoutParams = layoutParams
                        isMarginIncreased = true   // margin 증가 표시
                    }

                    val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in)
                    overlay.startAnimation(anim)
                } else {
                    // slide_out_diagonal 애니메이션 적용 후 숨기기
                    val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out)
                    overlay.startAnimation(anim)
                    // 애니메이션 종료 후 visibility를 GONE으로 설정 (애니메이션 리스너 사용)
                    anim.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {}
                        override fun onAnimationRepeat(animation: Animation?) {}
                        override fun onAnimationEnd(animation: Animation?) {
                            overlay.visibility = View.GONE
                        }
                    })
                }
            }
            val spannable = SpannableString(helpOverlayText.text)
            val wordsToHighlight = listOf("오타 수정 기능", "반복 수정", "흐름", "적합성 판단", "성장 가능성")

            for (word in wordsToHighlight) {
                val start = spannable.indexOf(word)
                if (start >= 0) {
                    spannable.setSpan(StyleSpan(Typeface.BOLD), start, start + word.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannable.setSpan(ForegroundColorSpan(Color.RED), start, start + word.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            helpOverlayText.text = spannable
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
    private fun callChatGPTToRevisePdfStreaming(pdfText: String) {
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
                You are an advanced proofreading system and a senior HR manager with 10 years of experience in hiring software developers.
                The user will provide PDF content and an additional prompt. Your job is to provide corrected or improved text with grammatical accuracy and HR relevance.
                
                Important:
                Wrap changes in  tags and display revisions in red (e.g.,  ... ).
                Use  tags for paragraph separation.
                Separate self-introduction questions and answers clearly.
                Treat sentences ending with “바랍니다.” as questions, inserting a blank line before the answer.
                Output only valid HTML in Korean, with no extra commentary.
                
                Provide feedback focused on:
                - Field-specific feedback: Highlight relevant project experiences, technologies, and achievements for the applicant’s development field.
                - Technical stack relevance: Ensure mentioned tools are current and experiences are clear.
                - Logical flow: Maintain a coherent narrative from motivation to experience, strengths, and aspirations.
                - Soft skills: Emphasize teamwork, problem-solving, and communication.
                - Company fit: Align applicant’s strengths with the company’s culture and requirements.
                - Growth potential: Highlight learning ability and enthusiasm for junior developers.
                - Unique strengths: Bring out distinctive experiences that set the applicant apart.
            """.trimIndent()
            )
        }
        val userMessage = JSONObject().apply {
            put("role", "user")
            put("content", pdfText)
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
                            binding.changeText.text = errorMsg
                        }
                        return@launch
                    }

                    val source: BufferedSource? = response.body?.source()
                    if (source == null) {
                        requireActivity().runOnUiThread {
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
                                            binding.uploadText.visibility = View.GONE
                                            binding.feedbackText.visibility = View.GONE
                                            binding.changeText.visibility = View.GONE
                                            binding.ivImage.visibility = View.GONE
                                            binding.tvPdfPath.visibility = View.GONE
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
                        binding.changeFeedback.text = "교정 완료! 아래는 미리보기에요."
                        binding.btnSubmit.text = "피드백 다시 받기"
                        binding.btnDownloadPdf.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
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


                        val cardParams = cardViewWeb.layoutParams
                        cardParams.height = newHeight + 3
                        cardViewWeb.layoutParams = cardParams
                        cardViewWeb.requestLayout()
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