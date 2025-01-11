package com.example.androidsecurityapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.androidsecurityapp.data.api.RetrofitInstance
import com.example.androidsecurityapp.data.responses.ApiResponse
import com.example.androidsecurityapp.databinding.ActivityHomeBinding
import com.example.androidsecurityapp.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        val logoutButton = binding.logoutButton

        // Fetch dashboard data
        fetchHomeData()

        logoutButton.setOnClickListener {
            // Clear JWT token
            TokenManager.clearToken(this)

            Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show()

            // Navigate back to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Fetches protected data from the backend using the stored JWT.
     */
    private fun fetchHomeData() {
        binding.progressBar.visibility = View.VISIBLE
        binding.logoutButton.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = TokenManager.getToken(this@HomeActivity)
                if (token.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        binding.logoutButton.isEnabled = true

                        Toast.makeText(
                            this@HomeActivity,
                            "Authentication token missing.",
                            Toast.LENGTH_SHORT
                        ).show()
                        navigateToLogin()
                    }
                    return@launch
                }

                val authHeader = "Bearer $token"
                val response = RetrofitInstance.api.getHome(authHeader)

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.logoutButton.isEnabled = true

                    if (response.isSuccessful && response.body() != null) {
                        val apiResponse: ApiResponse = response.body()!!
                        binding.welcomeText.text = apiResponse.message
                    } else {
                        // Handle unauthorized or other errors
                        val errorMessage = response.errorBody()?.string() ?: "Failed to fetch data."
                        Toast.makeText(this@HomeActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        navigateToLogin()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.logoutButton.isEnabled = true

                    e.printStackTrace()
                    Toast.makeText(
                        this@HomeActivity,
                        "An error occurred. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * Navigates the user back to the MainActivity (Login screen).
     */
    private fun navigateToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}