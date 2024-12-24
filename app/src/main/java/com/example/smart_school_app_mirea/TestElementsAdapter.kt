package com.example.smart_school_app_mirea

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView



class TestElementsAdapter(var items: List<TestElement>, var context: Context)
    : RecyclerView.Adapter<TestElementsAdapter.MyViewHolder>() {

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val question: TextView = view.findViewById(R.id.test_element_question)
        val userAnswer: EditText = view.findViewById(R.id.test_element_user_answer)
        val result: TextView = view.findViewById(R.id.test_element_result)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.test_element, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentQuestion = items[position] // Получаем текущий вопрос

        var needHold = false
        if (currentQuestion.user_answer != null) {
            needHold = true
        }

        holder.question.text = currentQuestion.title
        if (needHold) {
            holder.userAnswer.setText(currentQuestion.user_answer)
            holder.userAnswer.isEnabled = false
        }

        holder.userAnswer.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Сохраняем ответ в модели данных
                currentQuestion.user_answer = s?.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        if (needHold) {
            // Режим просмотра: показываем результат
            holder.result.visibility = View.VISIBLE
            if (currentQuestion.score!! >= 1) {
                holder.result.text = "Верно! ${currentQuestion.score} балл"
                holder.result.setTextColor(context.getColor(android.R.color.holo_green_dark))
            } else if (currentQuestion.score!! == 0) {
                holder.result.text = "Неверно, ${currentQuestion.score} баллов"
                holder.result.setTextColor(context.getColor(android.R.color.holo_red_dark))
            }
        } else {
            // Режим прохождения (needHld == false): скрываем результат
            holder.result.visibility = View.GONE
        }
    }

    fun getQuestionsWithAnswers(): List<TestElement> {
        return items
    }

}