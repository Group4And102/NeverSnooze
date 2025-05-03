package com.example.neversnooze

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ObjectPromptActivity : AppCompatActivity() {

    private val targetObjects = listOf("chair", "laptop", "keyboard", "tv", "mouse", "cell phone")
    private lateinit var selectedObject: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_object_prompt)

        selectedObject = targetObjects.random()

        val promptText = findViewById<TextView>(R.id.targetPrompt)
        val startButton = findViewById<Button>(R.id.startChallengeButton)

        promptText.text = "Find a \"$selectedObject\"!"

        startButton.setOnClickListener {
            val intent = Intent(this, ObjectDetection::class.java)
            intent.putExtra("TARGET_OBJECT", selectedObject)
            startActivity(intent)
            finish()
        }
    }
}
