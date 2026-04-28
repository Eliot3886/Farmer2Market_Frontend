package com.bignerdrange.farmer2market

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)
        val etNewUsername = findViewById<TextInputEditText>(R.id.etNewUsername)
        val etNewPassword = findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnReset = findViewById<MaterialButton>(R.id.btnResetPassword)
        val tvBack = findViewById<TextView>(R.id.tvBackToLogin)

        btnReset.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            val newUsername = etNewUsername.text.toString().trim()
            val newPass = etNewPassword.text.toString().trim()
            val confirmPass = etConfirmPassword.text.toString().trim()

            if (phone.isEmpty() || newUsername.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Snackbar.make(btnReset, "Please fill all fields", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (newPass != confirmPass) {
                Snackbar.make(btnReset, "Passwords do not match", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            btnReset.isEnabled = false

            val data = mapOf(
                "phone_number" to phone, 
                "new_username" to newUsername, 
                "new_password" to newPass
            )
            RetrofitClient.instance.resetPassword(data).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Snackbar.make(btnReset, "Password reset successfully!", Snackbar.LENGTH_LONG).show()
                        finish()
                    } else {
                        btnReset.isEnabled = true
                        Snackbar.make(btnReset, "User not found or error occurred", Snackbar.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    btnReset.isEnabled = true
                    Snackbar.make(btnReset, "Connection Error", Snackbar.LENGTH_LONG).show()
                }
            })
        }

        tvBack.setOnClickListener {
            finish()
        }
    }
}
