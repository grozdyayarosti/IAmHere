package com.example.iamhere

import DatabaseHelper
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity

class RemoveEmailActivity : ComponentActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remove_email)

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
                dbHelper.removeEmail(newEmail) -> {
                    val resultIntent = Intent()
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
                else -> {
                    Toast.makeText(this, "Ошибка", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}
