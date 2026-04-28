package com.bignerdrange.farmer2market

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        var doubleBackToExitPressedOnce = false
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finish()
                    return
                }
                doubleBackToExitPressedOnce = true
                android.widget.Toast.makeText(this@MainActivity, "Please click BACK again to exit", android.widget.Toast.LENGTH_SHORT).show()
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
            }
        })
        
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("access_token", null)
        val role = prefs.getString("role", "Buyer")

        if (token == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Set token for API calls
        RetrofitClient.setToken(token)
        
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        
        // Customize menu based on role
        when (role) {
            "Farmer" -> {
                // Keep default menu for farmer
            }
            "Admin" -> {
                // Admin can see Everything or specific? User said Admin is not a farmer.
                // So remove 'Add Product' from Admin too.
                bottomNav.menu.removeItem(R.id.nav_add)
            }
            else -> { // Buyer
                bottomNav.menu.removeItem(R.id.nav_add)
            }
        }

        // Default Fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_add -> loadFragment(AddProductFragment())
                R.id.nav_profile -> loadFragment(ProfileFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }
}
