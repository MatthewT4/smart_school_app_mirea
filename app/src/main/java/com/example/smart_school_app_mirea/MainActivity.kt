package com.example.smart_school_app_mirea

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import okhttp3.*
import com.google.gson.Gson
import java.io.IOException

import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null) // null будет
        if (token != null) {
            val intent = Intent(this, MyCourses::class.java)
            startActivity(intent)
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val userEmail: EditText = findViewById(R.id.reg_email)
        val userPass: EditText = findViewById(R.id.reg_pass)
        val button: Button = findViewById(R.id.reg_button)

        button.setOnClickListener {
            val email = userEmail.text.toString().trim()
            val password = userPass.text.toString().trim()

            if (email == "" || password == "")
                Toast
                    .makeText(this, "Должны быть заполнены все поля", Toast.LENGTH_LONG)
                    .show()
            else
                sendPostRequest(email, password) { result ->
                    runOnUiThread {
                        result.onSuccess { token ->
                            val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("auth_token", token)
                            editor.apply()
                            Toast
                                .makeText(this, "Token ${token}", Toast.LENGTH_LONG)
                                .show()
                        }.onFailure { exception ->
                            Log.e("LoginError", "Request failed: ${exception.message}", exception)
                            when (exception.message) {
                                "User not found: code 404" -> {
                                    // Показать специфичное сообщение для 400
                                    Toast
                                        .makeText(this, "Неверный логин/пароль", Toast.LENGTH_LONG)
                                        .show()
                                }
                                else -> {
                                    Toast
                                        .makeText(this, "Произошла ошибка, попробуйте позднее", Toast.LENGTH_LONG)
                                        .show()
                                }
                            }
                        }
                    }
                }
        }
    }
}

data class LoginRequest(val email: String, val password: String)

fun sendPostRequest(email: String, password: String, callback: (Result<String>) -> Unit) {
    val client = OkHttpClient()
    val url = "http://10.0.2.2:8080/auth/signin"

    val loginRequest = LoginRequest(email, password)
    val gson = Gson()
    val jsonString = gson.toJson(loginRequest)

    val requestBody = jsonString.toRequestBody("application/json; charset=utf-8".toMediaType())

    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            callback(Result.failure(e))
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                when (response.code) {
                    200 -> { // Успешный ответ
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            try {
                                val jsonResponse = gson.fromJson(responseBody, JsonObject::class.java)
                                val token = jsonResponse.get("token").asString
                                callback(Result.success(token))
                            } catch (e: Exception) {
                                callback(Result.failure(IOException("Error parsing JSON response", e)))
                            }
                        } else {
                            callback(Result.failure(IOException("Response body is null")))
                        }
                    }
                    404 -> { // Ошибка клиента
                        callback(Result.failure(IOException("User not found: code 404")))
                    }
                    else -> { // Любой другой неуспешный код ответа
                        callback(Result.failure(IOException("Unexpected response code: ${response.code}")))
                    }
                }
            }
        }
    })
}