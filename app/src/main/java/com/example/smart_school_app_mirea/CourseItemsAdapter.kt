package com.example.smart_school_app_mirea

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

import okhttp3.*
import com.google.gson.Gson
import java.io.IOException

import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody


class CourseItemsAdapter(var items: List<Course>, var context: Context)
    : RecyclerView.Adapter<CourseItemsAdapter.MyViewHolder>() {

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.course_item_title)
        val desc: TextView = view.findViewById(R.id.course_item_desc)
        val button: MaterialButton = view.findViewById(R.id.course_item_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.course_in_list, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val courseID = items[position].id

        holder.title.text = items[position].title
        holder.desc.text = items[position].description

        var buttonText = "Подключиться"
        if (items[position].user_invited_in_course == true) {
            buttonText = "Перейти"
            holder.button.setTextColor(context.getColor(R.color.text_secondary))
        } else {
            holder.button.setTextColor(context.getColor(R.color.primary))
        }
        holder.button.text = buttonText
        holder.button.setOnClickListener{
            val intent = Intent(context, CoursePage::class.java)
            intent.putExtra("courseID", courseID)
            context.startActivity(intent)
        }
    }
}