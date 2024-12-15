package com.example.smart_school_app_mirea

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CourseElementsAdapter(var items: List<CourseElement>, var context: Context)
    : RecyclerView.Adapter<CourseElementsAdapter.MyViewHolder>() {

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val elementType: TextView = view.findViewById(R.id.course_element_type)
        val title: TextView = view.findViewById(R.id.course_element_title)
        val button: Button = view.findViewById(R.id.course_element_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.course_element, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var elementTypeText = ""
        var elementTypeColor = Color.parseColor("#ffabab")
        if (items[position].elementType == ElementType.Topic) {
            elementTypeText = "Теория"
        } else {
            elementTypeText = "Практика"
            elementTypeColor = Color.parseColor("#759cff")
        }
        holder.elementType.text = elementTypeText
        holder.elementType.setBackgroundColor(elementTypeColor)
        holder.title.text = items[position].title
    }
}