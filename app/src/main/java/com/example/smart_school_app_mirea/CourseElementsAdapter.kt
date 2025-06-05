package com.example.smart_school_app_mirea

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
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
        Log.d("CourseElementsAdapter", "Creating view holder")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.course_element, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        Log.d("CourseElementsAdapter", "Item count: ${items.count()}")
        return items.count()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Log.d("CourseElementsAdapter", "Binding view holder at position $position")
        var elementTypeText = ""
        var elementTypeColor = Color.parseColor("#ffabab")
        var buttonColor = Color.parseColor("#ffabab")
        if (items[position].elementType == ElementType.Topic) {
            elementTypeText = "Теория"
            holder.button.setOnClickListener{
                val intent = Intent(context, TopicActivity::class.java)
                intent.putExtra("topicTitle", items[position].title)
                intent.putExtra("topicBody", items[position].topicBody!!)
                context.startActivity(intent)
            }
        } else {
            elementTypeText = "Практика"
            elementTypeColor = Color.parseColor("#759cff")
            buttonColor = Color.parseColor("#759cff")
            var buttonText = "Выполнить"
            if (items[position].maxScore != null) {
                buttonColor = Color.parseColor("#bababa")
                buttonText = "Посмотреть"
            }
            holder.button.text = buttonText
            holder.button.setOnClickListener{
                val intent = Intent(context, TestActivity::class.java)
                intent.putExtra("testID", items[position].id)
                context.startActivity(intent)
            }
        }
        holder.elementType.text = elementTypeText
        holder.elementType.setBackgroundColor(elementTypeColor)
        holder.title.text = items[position].title
        holder.button.setBackgroundColor(buttonColor)
        Log.d("CourseElementsAdapter", "Bound element: ${items[position].title}")
    }
}