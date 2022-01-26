package com.example.studenttally7.ui

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.studenttally7.R
import com.example.studenttally7.databinding.ActivityStartingBinding

class StartingActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityStartingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        sharedPreferences = getSharedPreferences("TallyAppPrefs", MODE_PRIVATE)
        val firstTime = sharedPreferences.getBoolean("firstTime", true)

        if (!firstTime) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.buttonSelectRoleStudent.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.putBoolean("firstTime", false)
            editor.putString("role", "student")
            editor.apply()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.buttonSelectRoleTeacher.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.putBoolean("firstTime", false)
            editor.putString("role", "teacher")
            editor.apply()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}