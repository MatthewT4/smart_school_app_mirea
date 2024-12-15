package com.example.smart_school_app_mirea

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException


class CoursePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_course_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}

data class Topic(
    val id: String,
    val courseId: String,
    val title: String,
    val body: String
)

data class Element(
    val elementId: String,
    val elementType: String,
    val index: Int
)

data class CourseDetails(
    val id: String,
    val title: String,
    val description: String,
    val userInvitedInCourse: Boolean,
    val topics: List<Topic>,  // Обратите внимание: тип - List<Topic>
    val elements: List<Element>
)

fun sendGetCourseDetailsRequest(courseId: String, token: String, callback: (Result<CourseDetails>) -> Unit) {
    val client = OkHttpClient()
    val url = "http://10.0.2.2:8080/courses/$courseId"

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

                val responseBody = response.body?.string() ?: return callback(Result.failure(IOException("Response body is null")))

                try {
                    val gson = Gson()

                    // Прямое парсинг JSON в объект CourseDetails
                    val courseDetails: CourseDetails = gson.fromJson(responseBody, CourseDetails::class.java)

                    // Создаем Map для topics
                    val topicsMap = courseDetails.topics.associateBy { it.id }

                    // Обновляем объект CourseDetails с новым Map
                    val result = courseDetails.copy(topics = topicsMap)

                    callback(Result.success(result))
                } catch (e: Exception) {
                    callback(Result.failure(IOException("Error parsing JSON response", e)))
                }
            }
        }
    })
}