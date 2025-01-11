package com.example.androidsecurityapp

import DatabaseHelper
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.androidsecurityapp.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
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
        val loginButton = binding.loginButton
        val registerText = binding.registerText

        loginButton.setOnClickListener {
            loginButton.setOnClickListener {
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

                // Perform login operation
                performLogin(username, password)
            }
        }

        registerText.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Handles the login process by validating credentials against the database.
     *
     * @param username The username entered by the user.
     * @param password The password entered by the user.
     */
    private fun performLogin(username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val isValid = dbHelper.checkLogin(username, password)

            withContext(Dispatchers.Main) {
                if (isValid) {
                    // Login successful
                    Toast.makeText(this@MainActivity, "Login successful!", Toast.LENGTH_SHORT)
                        .show()

                    // Navigate to the HomeActivity
                    val intent = Intent(this@MainActivity, HomeActivity::class.java)
                    // Pass user information to the next activity
                    intent.putExtra("USERNAME", username)
                    startActivity(intent)
                    finish() // Close the MainActivity so user can't return to it via back button
                } else {
                    // Login failed
                    Toast.makeText(
                        this@MainActivity,
                        "Invalid username or password.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}