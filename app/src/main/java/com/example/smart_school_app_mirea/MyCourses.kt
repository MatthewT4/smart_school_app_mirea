package com.example.smart_school_app_mirea

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException


class MyCourses : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_courses)

        val itemsList: RecyclerView = findViewById(R.id.my_courses_items_list)
        val items = arrayListOf<Course>()

        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", "")

        sendGetRequest(token!!) { result ->
            runOnUiThread {
                result.onSuccess { courses ->
                    itemsList.layoutManager = LinearLayoutManager(this)
                    itemsList.adapter = CourseItemsAdapter(courses, this)
                }.onFailure { exception ->
                    Toast
                        .makeText(this, "Произошла ошибка, попробуйте позднее", Toast.LENGTH_LONG)
                        .show()
                    }
                }
            }
        }
    }

fun sendGetRequest(token: String, callback: (Result<ArrayList<Course>>) -> Unit) {
    val client = OkHttpClient()
    val url = "http://10.0.2.2:8080/courses?my=true"

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
                    val listType = object : TypeToken<List<Course>>() {}.type
                    // Парсинг и преобразование в ArrayList
                    val courses: ArrayList<Course> = gson.fromJson<List<Course>>(responseBody, listType)
                        .toCollection(ArrayList())

                    callback(Result.success(courses))
                } catch (e: Exception) {
                    callback(Result.failure(IOException("Error parsing JSON response", e)))
                }
            }
        }
    })
}