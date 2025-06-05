package com.example.smart_school_app_mirea

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


class CoursePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val courseID = intent.getStringExtra("courseID")

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_course_page)

        val title: TextView = findViewById(R.id.course_title)
        val description: TextView = findViewById(R.id.course_desc)
        val courseElements: RecyclerView = findViewById(R.id.course_elements)

        // Устанавливаем LayoutManager для RecyclerView
        courseElements.layoutManager = LinearLayoutManager(this)

        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", "")

        inviteInCourse(courseID!!, token!!)

        sendGetCourseRequest(courseID!!, token!!) { result ->
            runOnUiThread {
                result.onSuccess { courseDetail ->
                    Log.d("CoursePage", "Received course detail: $courseDetail")
                    Log.d("CoursePage", "Topics count: ${courseDetail.topics.size}")
                    Log.d("CoursePage", "Tests count: ${courseDetail.tests.size}")
                    Log.d("CoursePage", "Elements count: ${courseDetail.elements.size}")

                    val elements = convertCourseDetailToCourseElements(courseDetail)
                    Log.d("CoursePage", "Converted elements count: ${elements.size}")
                    
                    title.text = courseDetail.title
                    description.text = courseDetail.description
                    courseElements.adapter = CourseElementsAdapter(elements, this)
                }.onFailure { exception ->
                    Log.e("CoursePage", "Error loading course", exception)
                    Toast
                        .makeText(this, "Произошла ошибка, попробуйте позднее", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    fun inviteInCourse(courseID: String, token: String) {
        inviteInCourseRequest(courseID, token) { result ->
            runOnUiThread {
                result.onSuccess { courseDetail ->
                    print("Invited in course")
                }.onFailure { exception ->
                    println(exception)
                    Toast
                        .makeText(this, "Произошла ошибка, попробуйте позднее", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }
}


fun inviteInCourseRequest(courseID: String, token: String, callback: (Result<Unit>) -> Unit) {
    println("token")
    println(token)
    val client = OkHttpClient()
    val url = "http://10.0.2.2:8080/courses/$courseID/invite"

    val requestBody = "".toRequestBody("application/json; charset=utf-8".toMediaType())

    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .addHeader("Authorization", token)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            callback(Result.failure(e))
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                when (response.code) {
                    204 -> { // Успешный ответ
                        callback(Result.success(Unit))
                    }
                    404 -> { // Ошибка клиента
                        callback(Result.failure(IOException("Course not found: code 404")))
                    }
                    else -> { // Любой другой неуспешный код ответа
                        callback(Result.failure(IOException("Unexpected response code: ${response.code}")))
                    }
                }
            }
        }
    })
}

data class Topic(
    val id: String,
    val course_id: String,
    val title: String,
    val body: String
)

data class Element(
    val element_id: String,
    val element_type: String,
    val index: Int
)

data class TestElement(
    val id: String,
    val test_id: String,
    val title: String,
    val correct_answer: String?,
    val index: Int,
    var user_answer: String?,
    val score: Int?
)

data class Test(
    val id: String,
    val course_id: String,
    val title: String,
    val elements: List<TestElement>,
    val max_score: Int?,
    val result_score: Int?
)

data class CourseDetail(
    val id: String,
    val title: String,
    val description: String,
    val user_invited_in_course: Boolean,
    val topics: List<Topic>,
    val elements: List<Element>,
    val tests: List<Test>
)

fun sendGetCourseRequest(courseId: String, token: String, callback: (Result<CourseDetail>) -> Unit) {
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

                    // Парсить ответ в объект CourseDetail
                    val courseDetail = gson.fromJson(responseBody, CourseDetail::class.java)

                    callback(Result.success(courseDetail))
                } catch (e: Exception) {
                    callback(Result.failure(IOException("Error parsing JSON response", e)))
                }
            }
        }
    })
}

fun convertCourseDetailToCourseElements(courseDetail: CourseDetail): List<CourseElement> {
    val elements = mutableListOf<CourseElement>()
    
    // Add topics
    courseDetail.topics.forEach { topic ->
        elements.add(
            CourseElement(
                id = topic.id,
                title = topic.title,
                elementType = ElementType.Topic,
                topicBody = topic.body
            )
        )
    }
    
    // Add tests
    courseDetail.tests.forEach { test ->
        elements.add(
            CourseElement(
                id = test.id,
                title = test.title,
                elementType = ElementType.Test,
                maxScore = test.max_score,
                resultScore = test.result_score,
                testElements = test.elements
            )
        )
    }
    
    // Sort by index if available
    return elements.sortedBy { it.id }
}