package com.example.smart_school_app_mirea

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CourseItemsAdapter(var items: List<Course>, var context: Context)
    : RecyclerView.Adapter<CourseItemsAdapter.MyViewHolder>() {

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.course_item_title)
        val desc: TextView = view.findViewById(R.id.course_item_desc)
        val button: Button = view.findViewById(R.id.course_item_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.course_in_list, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.title.text = items[position].title
        holder.desc.text = items[position].description

        var buttonText = "Подключиться"
        var buttonColor = Color.RED
        if (items[position].user_invited_in_course == true) {
            buttonText = "Перейти"
            buttonColor = Color.GRAY
        }
        holder.button.text = buttonText
        holder.button.setBackgroundColor(buttonColor)


    }
}