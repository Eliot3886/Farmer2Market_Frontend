package com.bignerdrange.farmer2market

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<TextInputEditText>(R.id.etUsername)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val tvForgot = findViewById<TextView>(R.id.tvForgotPassword)

        tvForgot.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Snackbar.make(btnLogin, "Please fill all fields", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false

            val data = mapOf("username" to username, "password" to password)
            RetrofitClient.instance.login(data).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful) {
                        val auth = response.body() ?: return
                        saveSession(auth)
                        android.widget.Toast.makeText(this@LoginActivity, "You are successfully logged in", android.widget.Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        btnLogin.isEnabled = true
                        Snackbar.make(btnLogin, "Invalid Credentials", Snackbar.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    btnLogin.isEnabled = true
                    Snackbar.make(btnLogin, "Error: ${t.message}. Check your IP Config in RetrofitClient.", Snackbar.LENGTH_LONG).show()
                }
            })
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun saveSession(auth: AuthResponse) {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("access_token", auth.access)
            putString("refresh_token", auth.refresh)
            putString("username", auth.user.username)
            putString("full_name", auth.user.full_name)
            putString("role", auth.user.role)
            putString("farm_name", auth.user.farm_name)
            putInt("user_id", auth.user.id)
            apply()
        }
        RetrofitClient.setToken(auth.access)
    }
}
