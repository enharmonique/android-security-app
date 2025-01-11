package com.example.androidsecurityapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.androidsecurityapp.data.api.RetrofitInstance
import com.example.androidsecurityapp.data.requests.RegisterRequest
import com.example.androidsecurityapp.data.responses.ApiResponse
import com.example.androidsecurityapp.databinding.ActivityRegisterBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {
    // Regex for usernames: 3-15 characters, letters, numbers, underscores
    private val USERNAME_PATTERN = Regex("^[a-zA-Z0-9_]{3,15}$")

    // Regex for passwords: at least 8 characters, one uppercase, one lowercase, one digit, one special character
    private val PASSWORD_PATTERN =
        Regex("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}\$")

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI components
        val usernameInput = binding.usernameInput
        val passwordInput = binding.passwordInput
        val registerButton = binding.registerButton
        val loginText = binding.loginText

        registerButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (username.isEmpty()) {
                usernameInput.error = "Username is required"
                usernameInput.requestFocus()
                return@setOnClickListener
            }

            if (!isValidUsername(username)) {
                usernameInput.error =
                    "Username must be 3-15 characters and contain only letters, numbers, or underscores"
                usernameInput.requestFocus()
                return@setOnClickListener
            }


            if (password.isEmpty()) {
                passwordInput.error = "Password is required"
                passwordInput.requestFocus()
                return@setOnClickListener
            }

            if (!isValidPassword(password)) {
                passwordInput.error =
                    "Password must be at least 8 characters and include uppercase, lowercase, number, and special character"
                passwordInput.requestFocus()
                return@setOnClickListener
            }

            // Hide keyboard
            hideKeyboard()

            // Perform registration operation
            performRegistration(username, password)
        }

        loginText.setOnClickListener {
            // Navigate back to MainActivity (Login screen)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close RegisterActivity so user can't return via back button
        }
    }

    /**
     * Handles the registration process by sending user details to the backend API.
     *
     * @param username The username entered by the user.
     * @param password The password entered by the user.
     */
    private fun performRegistration(username: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.registerButton.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val registerRequest = RegisterRequest(username, password)
                val response = RetrofitInstance.api.registerUser(registerRequest)

                withContext(Dispatchers.Main) {
                    // Hide loading indicator
                    binding.progressBar.visibility = View.GONE
                    binding.registerButton.isEnabled = true

                    if (response.isSuccessful && response.body() != null) {
                        val apiResponse: ApiResponse = response.body()!!
                        Toast.makeText(
                            this@RegisterActivity,
                            apiResponse.message,
                            Toast.LENGTH_SHORT
                        ).show()

                        // Navigate back to MainActivity (Login screen)
                        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Handle registration failure
                        val errorMessage = response.errorBody()?.string() ?: "Registration failed."
                        Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Hide loading indicator
                    binding.progressBar.visibility = View.GONE
                    binding.registerButton.isEnabled = true

                    e.printStackTrace()
                    Toast.makeText(
                        this@RegisterActivity,
                        "An error occurred. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * Hides the keyboard from the screen.
     */
    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        var view = currentFocus
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * Enables edge-to-edge UI for a modern, immersive experience.
     */
    private fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }


    private fun isValidUsername(username: String): Boolean {
        return USERNAME_PATTERN.matches(username)
    }

    private fun isValidPassword(password: String): Boolean {
        return PASSWORD_PATTERN.matches(password)
    }
}