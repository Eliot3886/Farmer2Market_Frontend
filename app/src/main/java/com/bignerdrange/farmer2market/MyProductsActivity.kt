package com.bignerdrange.farmer2market

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyProductsActivity : AppCompatActivity() {

    private lateinit var rvProducts: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var adapter: ProductAdapter
    private var ownerId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_products)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        rvProducts = findViewById(R.id.rvMyProducts)
        swipeRefresh = findViewById(R.id.swipeRefresh)

        rvProducts.layoutManager = LinearLayoutManager(this)
        
        ownerId = intent.getIntExtra("owner_id", -1).takeIf { it != -1 }
        val farmName = intent.getStringExtra("farm_name")
        if (farmName != null) {
            toolbar.title = "$farmName's Market"
        }
        
        // Passing true for 'isHistory' so edit/delete buttons are shown
        adapter = ProductAdapter(mutableListOf(), { product ->
            // Edit logic - for now just toast or open AddProductFragment with data
            Toast.makeText(this, "Edit ${product.name}", Toast.LENGTH_SHORT).show()
        }, { product ->
            deleteProduct(product)
        }, isHistory = true)
        rvProducts.adapter = adapter

        swipeRefresh.setOnRefreshListener { loadMyProducts() }

        loadMyProducts()
    }

    private fun loadMyProducts() {
        swipeRefresh.isRefreshing = true
        RetrofitClient.instance.getMyProducts(ownerId).enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                swipeRefresh.isRefreshing = false
                if (response.isSuccessful) {
                    adapter.updateData(response.body() ?: emptyList())
                }
            }
            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                swipeRefresh.isRefreshing = false
                Toast.makeText(this@MyProductsActivity, "Error loading history", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteProduct(product: Product) {
        product.id?.let { id ->
            RetrofitClient.instance.deleteProduct(id).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        loadMyProducts()
                        Toast.makeText(this@MyProductsActivity, "Product deleted", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {}
            })
        }
    }
}
