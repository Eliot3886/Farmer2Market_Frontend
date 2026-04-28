package com.bignerdrange.farmer2market

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etFullName = findViewById<TextInputEditText>(R.id.etFullName)
        val etUsername = findViewById<TextInputEditText>(R.id.etUsername)
        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val etFarmName = findViewById<TextInputEditText>(R.id.etFarmName)
        val tvAutoLocation = findViewById<TextView>(R.id.tvAutoLocation)
        val rgRole = findViewById<RadioGroup>(R.id.rgRole)
        val llFarmerFields = findViewById<LinearLayout>(R.id.llFarmerFields)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        var detectedLocation = "Harare, Zimbabwe"
        LocationHelper.detectLocation(this) { loc ->
            detectedLocation = loc
            tvAutoLocation.text = "📍 Location: $loc"
        }

        rgRole.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbFarmer) {
                llFarmerFields.visibility = View.VISIBLE
            } else {
                llFarmerFields.visibility = View.GONE
            }
        }

        btnRegister.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            val farmName = etFarmName.text.toString().trim()
            
            val selectedRoleId = rgRole.checkedRadioButtonId
            val role = if (selectedRoleId == R.id.rbFarmer) "Farmer" else "Buyer"

            if (fullName.isEmpty() || username.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Snackbar.make(btnRegister, "Please fill all required fields", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Snackbar.make(btnRegister, "Passwords do not match", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (phone.length != 10) {
                Snackbar.make(btnRegister, "Phone number must be 10 digits", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            btnRegister.isEnabled = false

            val data = mutableMapOf(
                "full_name" to fullName,
                "username" to username,
                "phone_number" to phone,
                "location" to detectedLocation,
                "password" to password,
                "confirm_password" to confirmPassword,
                "role" to role,
                "farm_name" to farmName
            )

            RetrofitClient.instance.register(data).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        android.widget.Toast.makeText(this@RegisterActivity, "You are successfully registered", android.widget.Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        btnRegister.isEnabled = true
                        val errorBodyStr = response.errorBody()?.string() ?: ""
                        val errorMsg = when {
                            errorBodyStr.contains("phone_number") -> "This phone number is already registered."
                            errorBodyStr.contains("username") -> "This username is already taken."
                            else -> "Registration failed. Please check your details."
                        }
                        Snackbar.make(btnRegister, errorMsg, Snackbar.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    btnRegister.isEnabled = true
                    Snackbar.make(btnRegister, "Error: ${t.message}", Snackbar.LENGTH_LONG).show()
                }
            })
        }

        tvLogin.setOnClickListener {
            finish()
        }
    }
}
