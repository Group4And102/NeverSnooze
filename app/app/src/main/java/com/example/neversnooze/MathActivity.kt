package com.example.neversnooze

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.neversnooze.R
import kotlin.random.Random

class MathActivity : AppCompatActivity() {
    private lateinit var problemCounter: TextView
    private lateinit var mathProblem: TextView
    private lateinit var answerInput: EditText
    private lateinit var submitButton: Button

    private var currentProblem = 1
    private val totalProblems = 3 // Reduced to 3 problems for better UX
    private var correctAnswer = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_math)

        problemCounter = findViewById(R.id.problemCounter)
        mathProblem = findViewById(R.id.mathProblem)
        answerInput = findViewById(R.id.answerInput)
        submitButton = findViewById(R.id.submitButton)

        submitButton.setOnClickListener {
            submitAnswer()
        }

        answerInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitAnswer()
                true
            } else {
                false
            }
        }

        showNextProblem()
    }

    private fun generateMathProblem(): Pair<String, Int> {
        val num1 = Random.nextInt(10, 100)
        val num2 = Random.nextInt(10, 100)
        return Pair("$num1 + $num2", num1 + num2)
    }

    private fun showNextProblem() {
        if (currentProblem <= totalProblems) {
            val (problemText, answer) = generateMathProblem()
            correctAnswer = answer
            problemCounter.text = "$currentProblem/$totalProblems"
            mathProblem.text = problemText
            answerInput.text.clear()
            answerInput.requestFocus()
            currentProblem++
        } else {
            // Challenge completed successfully
            // Forwards all alarm details (for snoozing)
            val congrats = Intent(this, CongratulationsActivity::class.java).apply {
                putExtras(intent.extras ?: Bundle())
            }
            startActivity(congrats)
            finish()
        }
    }

    private fun submitAnswer() {
        val userInput = answerInput.text.toString()
        if (userInput.isNotBlank() && userInput.toIntOrNull() != null) {
            if (userInput.toInt() == correctAnswer) {
                Toast.makeText(this, "Correct ✅", Toast.LENGTH_SHORT).show()
                showNextProblem()
            } else {
                Toast.makeText(this, "Wrong ❌ Try again", Toast.LENGTH_SHORT).show()
                answerInput.text.clear()
                answerInput.requestFocus()
            }
        } else {
            Toast.makeText(this, "Enter a number!", Toast.LENGTH_SHORT).show()
        }
    }
} 