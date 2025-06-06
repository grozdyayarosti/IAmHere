package com.example.iamhere

import DatabaseHelper
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity

class AddEmailActivity : ComponentActivity() {
    private lateinit var dbHelper: DatabaseHelper

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_email)

        dbHelper = DatabaseHelper(this)
        val emailInput = findViewById<EditText>(R.id.newEmailInput)
        val saveButton = findViewById<Button>(R.id.addButton)

        saveButton.setOnClickListener {

            val newEmail = emailInput.text.toString().trim()

            when {
                newEmail.isEmpty() -> {
                    Toast.makeText(this, "Введите gmail", Toast.LENGTH_SHORT).show()
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches() -> {
                    Toast.makeText(this, "Введите корректный gmail", Toast.LENGTH_SHORT).show()
                }
                dbHelper.addEmail(newEmail) -> {
                    val resultIntent = Intent()
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
                else -> {
                    Toast.makeText(this, "Такой email уже существует", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}
