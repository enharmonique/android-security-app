package com.example.androidsecurityapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.androidsecurityapp.data.api.RetrofitInstance
import com.example.androidsecurityapp.data.requests.LoginRequest
import com.example.androidsecurityapp.data.responses.AuthResponse
import com.example.androidsecurityapp.databinding.ActivityMainBinding
import com.example.androidsecurityapp.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

                // Hide keyboard
                hideKeyboard()

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
     * Handles the login process by sending credentials to the backend API.
     *
     * @param username The username entered by the user.
     * @param password The password entered by the user.
     */
    private fun performLogin(username: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.loginButton.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.loginUser(LoginRequest(username, password))

                withContext(Dispatchers.Main) {
                    // Hide loading indicator
                    binding.progressBar.visibility = View.GONE
                    binding.loginButton.isEnabled = true

                    if (response.isSuccessful && response.body() != null) {
                        val authResponse: AuthResponse = response.body()!!
                        val token = authResponse.token

                        // Save the JWT token securely
                        TokenManager.saveToken(this@MainActivity, token)

                        Toast.makeText(this@MainActivity, "Login successful!", Toast.LENGTH_SHORT)
                            .show()

                        // Navigate to HomeActivity
                        val intent = Intent(this@MainActivity, HomeActivity::class.java)
                        // Pass user information to the next activity
                        intent.putExtra("USERNAME", username)
                        startActivity(intent)
                        finish()
                    } else {
                        // Handle login failure
                        val errorMessage = response.errorBody()?.string() ?: "Login failed."
                        Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Hide loading indicator
                    binding.progressBar.visibility = View.GONE
                    binding.loginButton.isEnabled = true

                    e.printStackTrace()
                    Toast.makeText(
                        this@MainActivity,
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