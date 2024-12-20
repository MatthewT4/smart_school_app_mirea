package com.example.smart_school_app_mirea

import android.os.Bundle
import android.text.Html
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TopicActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_topic)

        val title: TextView = findViewById(R.id.topic_title)
        val body: TextView = findViewById(R.id.topic_body)

        var fromRequestTitle = intent.getStringExtra("topicTitle")
        var fromRequestBody = intent.getStringExtra("topicBody")


        // В зависимости от версии Android, подход может отличаться
        val spannedBody = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(fromRequestBody, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(fromRequestBody)
        }

        title.text = fromRequestTitle
        body.text = spannedBody
    }
}