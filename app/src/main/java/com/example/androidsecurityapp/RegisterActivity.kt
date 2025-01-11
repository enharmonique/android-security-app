package com.example.androidsecurityapp

import DatabaseHelper
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.androidsecurityapp.databinding.ActivityRegisterBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize DatabaseHelper
        dbHelper = DatabaseHelper.getInstance(this)

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

            // Validate input fields
            if (username.isEmpty()) {
                usernameInput.error = "Username is required"
                usernameInput.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordInput.error = "Password is required"
                passwordInput.requestFocus()
                return@setOnClickListener
            }

            // Enforce password strength
            if (password.length < 6) {
                passwordInput.error = "Password must be at least 6 characters"
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
     * Handles the registration process by adding a new user to the database.
     *
     * @param username The username entered by the user.
     * @param password The password entered by the user.
     */
    private fun performRegistration(username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Attempt to add the user to the database
                val userId = dbHelper.addUser(
                    username,
                    password
                )

                withContext(Dispatchers.Main) {
                    if (userId != -1L) {
                        // Registration successful
                        Toast.makeText(
                            this@RegisterActivity,
                            "Registration successful! Please login.",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Navigate to MainActivity (Login screen)
                        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Close RegisterActivity
                    } else {
                        // Registration failed (likely due to duplicate username)
                        Toast.makeText(
                            this@RegisterActivity,
                            "Username already exists. Please choose another.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Handle exceptions (e.g., database errors)
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
}