package com.example.smart_school_app_mirea

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException


data class TestElementAnswer(
    val element_id: String,
    val answer: String,
)


class TestActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_test)
        renderUI()
    }
    fun renderUI() {
        val itemsList: RecyclerView = findViewById(R.id.test_elements)
        val button: Button = findViewById(R.id.test_button)
        val resultView: TextView = findViewById(R.id.test_result_textview)

        val testID = intent.getStringExtra("testID")

        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", "")

        sendGetTestRequest(testID!!, token!!) { result ->
            runOnUiThread {
                result.onSuccess { test ->
                    if (test.result_score != null) {
                        button.visibility = View.GONE
                        resultView.visibility = View.VISIBLE
                        resultView.text = "Результат ${test.result_score} / ${test.max_score}"
                    } else {
                        resultView.visibility = View.GONE
                        button.visibility = View.VISIBLE
                    }

                    itemsList.layoutManager = LinearLayoutManager(this)
                    val adapter = TestElementsAdapter(test.elements, this)
                    itemsList.adapter = adapter

                    button.setOnClickListener{
                        val answers = adapter.getQuestionsWithAnswers()
                        val testElementAnswers = answers.map { testElement ->
                            TestElementAnswer(
                                element_id = testElement.id,
                                answer = testElement.user_answer ?: ""
                            )
                        }
                        sendPostTestAnswersRequest(test.id, token, testElementAnswers) { result ->
                            result.onSuccess {
                                runOnUiThread {
                                    Toast.makeText(this, "Ответы отправлены успешно!", Toast.LENGTH_SHORT).show()
                                }
                                renderUI()
                            }.onFailure { exception ->
                                runOnUiThread {
                                    Toast.makeText(this, "Ошибка отправки: ${exception.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }.onFailure { exception ->
                    Toast
                        .makeText(this, "Произошла ошибка, попробуйте позднее", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }
}



fun sendGetTestRequest(testId: String, token: String, callback: (Result<Test>) -> Unit) {
    val client = OkHttpClient()
    val url = "http://10.0.2.2:8080/tests/$testId"

    val request = Request.Builder()
        .url(url)
        .addHeader("Authorization", token)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            callback(Result.failure(e))
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                if (!response.isSuccessful) {
                    callback(Result.failure(IOException("Unexpected response code: ${response.code}")))
                    return
                }

                val responseBody = response.body?.string() ?: return callback(Result.failure(
                    IOException("Response body is null")
                ))
                try {
                    val gson = Gson()

                    // Парсить ответ в объект CourseDetail
                    val test = gson.fromJson(responseBody, Test::class.java)

                    callback(Result.success(test))
                } catch (e: Exception) {
                    callback(Result.failure(IOException("Error parsing JSON response", e)))
                }
            }
        }
    })
}


fun sendPostTestAnswersRequest(
    testId: String,
    token: String,
    answers: List<TestElementAnswer>,
    callback: (Result<Unit>) -> Unit
) {
    val client = OkHttpClient()
    val url = "http://10.0.2.2:8080/tests/$testId"

    // Преобразуем массив ответов в JSON
    val gson = Gson()
    val jsonBody = gson.toJson(answers)

    // Создаём тело запроса
    val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

    // Создаём запрос
    val request = Request.Builder()
        .url(url)
        .post(requestBody) // Используем POST
        .addHeader("Authorization", token) // Добавляем токен
        .build()

    // Выполняем запрос
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            // Если запрос завершился с ошибкой
            callback(Result.failure(e))
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                // Проверяем статус ответа
                if (response.code == 204) {
                    // Успешный ответ (204)
                    callback(Result.success(Unit))
                } else {
                    // Обрабатываем ошибку (не 204)
                    callback(Result.failure(IOException("Unexpected response code: ${response.code}")))
                }
            }
        }
    })
}