package com.example.androidsecurityapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.androidsecurityapp.databinding.ActivityHomeBinding
import com.example.androidsecurityapp.databinding.ActivityMainBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val username = intent.getStringExtra("USERNAME") ?: "User"
        binding.welcomeText.text = "Welcome, $username!"

        //TODO
//        binding.logoutButton.setOnClickListener {
//            // Clear SharedPreferences
//            with(sharedPref.edit()) {
//                remove("USERNAME_KEY")
//                apply()
//            }
//
//            // Navigate back to MainActivity
//            val intent = Intent(this@HomeActivity, MainActivity::class.java)
//            startActivity(intent)
//            finish()
    }
}