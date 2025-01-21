package com.example.openaiprompt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var editTextPrompt: EditText
    private lateinit var btnSubmit: Button
    private lateinit var tvResult: TextView

    private val openAIApiKey = "저의 openai API key (개인보관하겠습니다)"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextPrompt = findViewById(R.id.editTextPrompt)
        btnSubmit = findViewById(R.id.btnSubmit)
        tvResult = findViewById(R.id.tvResult)

        btnSubmit.setOnClickListener {
            val userText = editTextPrompt.text.toString().trim()
            if (userText.isNotEmpty()) {
                callChatGPT(userText)
            } else {
                Toast.makeText(this, "글을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun callChatGPT(inputText: String) {
        val client = OkHttpClient()
        val url = "https://api.openai.com/v1/chat/completions"

        // 시스템 메시지: SSAFY가 포함되어 있으면 DETECTED_SSAFY, 아니면 NO_SSAFY
        val systemMessage = JSONObject()
        systemMessage.put("role", "system")
        // 영어로 작업해야 빨라서, 영어로 작성했습니다.
        systemMessage.put("content", "You are a classification system. If the user's text includes the word 'SSAFY', respond with 'DETECTED_SSAFY'. Otherwise, respond with 'NO_SSAFY'.")

        // 사용자 메시지
        val userMessage = JSONObject()
        userMessage.put("role", "user")
        // inputText는 내가 입력하는 값들입니다.
        userMessage.put("content", inputText)

        val messagesArray = JSONArray()
        messagesArray.put(systemMessage)
        messagesArray.put(userMessage)

        val jsonBody = JSONObject()
        // 어려운 프롬프트는 쓰지 않을 것 같아서 3.5 버전을 사용했습니다.
        jsonBody.put("model", "gpt-3.5-turbo")
        jsonBody.put("messages", messagesArray)

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = RequestBody.create(mediaType, jsonBody.toString())

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $openAIApiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                runOnUiThread {
                    tvResult.text = "Error: ${response.code}"
                }
            } else {
                val bodyString = response.body?.string() ?: ""
                parseChatGPTResponse(bodyString)
            }
            response.close()
        }
    }

    // GPT에서 주는 response를 저희가 원하는 값(String등) 으로 바꿔주는 함수입니다.
    private fun parseChatGPTResponse(responseJson: String) {
        val jsonObject = JSONObject(responseJson)
        val choices = jsonObject.getJSONArray("choices")
        val firstChoice = choices.getJSONObject(0)
        val messageObject = firstChoice.getJSONObject("message")
        // 이걸 기반으로 이제 SSAFY가 감지가 되는지 안 되는지 파악을 해서요
        val content = messageObject.getString("content").trim()
        Log.d("TAG", "parseChatGPTResponse: $content")

        // 쓰레드 돌려서 파악합니다.
        runOnUiThread {
            tvResult.text = "ChatGPT 응답: $content"
            if (content == "DETECTED_SSAFY") {
                AlertDialog.Builder(this)
                    .setTitle("알림")
                    .setMessage("SSAFY 단어가 감지되었습니다.")
                    .setPositiveButton("확인") { dialog, _ -> dialog.dismiss() }
                    .show()
            } else if (content == "NO_SSAFY") {
                Toast.makeText(this, "글이 등록되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "알 수 없는 응답: $content", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
