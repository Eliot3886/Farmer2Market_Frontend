package com.bignerdrange.farmer2market

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminActivity : AppCompatActivity() {

    private lateinit var rvUsers: RecyclerView
    private lateinit var tvFarmers: TextView
    private lateinit var tvBuyers: TextView
    private lateinit var tvProducts: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow)
        toolbar.setNavigationOnClickListener { finish() } 

        rvUsers = findViewById(R.id.rvAdminUsers)
        tvFarmers = findViewById(R.id.tvAdminFarmers)
        tvBuyers = findViewById(R.id.tvAdminBuyers)
        tvProducts = findViewById(R.id.tvAdminProducts)

        rvUsers.layoutManager = LinearLayoutManager(this)

        loadDashboard()
    }

    // Removed onCreateOptionsMenu and onOptionsItemSelected to hide right head side back arrow

    private fun loadDashboard() {
        RetrofitClient.instance.getAdminDashboard().enqueue(object : Callback<AdminDashboardData> {
            override fun onResponse(call: Call<AdminDashboardData>, response: Response<AdminDashboardData>) {
                if (response.isSuccessful) {
                    val data = response.body() ?: return
                    tvFarmers.text = data.stats.farmers.toString()
                    tvBuyers.text = data.stats.buyers.toString()
                    tvProducts.text = data.stats.products.toString()
                    
                    // Exclude the admin user themselves from the list
                    val filteredUsers = data.users.filter { it.username != "!admin" }
                    rvUsers.adapter = AdminUserAdapter(filteredUsers, { userId ->
                        deleteUser(userId)
                    }, { user ->
                        if (user.role == "Farmer") {
                            val intent = android.content.Intent(this@AdminActivity, MyProductsActivity::class.java)
                            intent.putExtra("owner_id", user.id)
                            intent.putExtra("farm_name", user.farm_name ?: user.full_name)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@AdminActivity, "This user is a Buyer", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
            override fun onFailure(call: Call<AdminDashboardData>, t: Throwable) {
                Toast.makeText(this@AdminActivity, "Error loading admin data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteUser(id: Int) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Are you sure you want to remove this user account? This will also remove their products.")
            .setPositiveButton("Delete") { _, _ ->
                RetrofitClient.instance.adminDeleteUser(id).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            loadDashboard()
                            Toast.makeText(this@AdminActivity, "User removed", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {}
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

class AdminUserAdapter(
    private val users: List<UserTokenData>,
    private val onDelete: (Int) -> Unit,
    private val onClick: (UserTokenData) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvAdminUserName)
        val role: TextView = view.findViewById(R.id.tvAdminUserRole)
        val phone: TextView = view.findViewById(R.id.tvAdminUserPhone)
        val date: TextView = view.findViewById(R.id.tvAdminUserDate)
        val btnDelete: ImageButton = view.findViewById(R.id.btnAdminDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_user, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.name.text = user.full_name
        holder.role.text = user.role
        holder.phone.text = user.phone_number
        holder.date.text = "Registered: ${user.date_joined?.substringBefore("T") ?: "N/A"}"
        holder.btnDelete.setOnClickListener { onDelete(user.id) }
        holder.itemView.setOnClickListener { onClick(user) }
    }
}
