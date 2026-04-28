package com.bignerdrange.farmer2market

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var recyclerView: RecyclerView
    private var allProducts = listOf<Product>()
    private lateinit var tvUnreadCount: TextView
    private lateinit var etSearch: EditText

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        val llSearchContainer = view.findViewById<View>(R.id.llSearchContainer)
        val tvHomeTitle = view.findViewById<TextView>(R.id.tvHomeTitle)
        etSearch = view.findViewById(R.id.etSearch)
        tvUnreadCount = view.findViewById(R.id.tvUnreadCount)
        
        val swipeRefresh = view.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh.setColorSchemeResources(R.color.primary)
        swipeRefresh.setOnRefreshListener {
            loadProducts(etSearch.text.toString())
            fetchUnreadCount()
        }

        updateHeader()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { 
                val query = s.toString()
                if (query.length >= 1 || query.isEmpty()) {
                    loadProducts(query) 
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        
        etSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                etSearch.clearFocus()
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(etSearch.windowToken, 0)
                true
            } else false
        }
        
        // Animated Search Hint Feature
        val hints = listOf("Search premium produce...", "Search organic tomatoes...", "Search fresh fruits...", "Search healthy grains...")
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        var hintIndex = 0
        handler.post(object : Runnable {
            override fun run() {
                if (view.context != null) {
                    etSearch.hint = hints[hintIndex]
                    hintIndex = (hintIndex + 1) % hints.size
                    handler.postDelayed(this, 3000)
                }
            }
        })

        setupChips(view)

        val btnInbox = view.findViewById<View>(R.id.btnInbox)
        btnInbox.setOnClickListener {
            startActivity(android.content.Intent(requireContext(), InboxActivity::class.java))
        }

        view.findViewById<View>(R.id.btnRefresh).setOnClickListener {
            loadProducts(etSearch.text.toString())
            fetchUnreadCount()
        }

        val fabScrollUp = view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabScrollUp)
        
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && fabScrollUp.visibility == View.GONE) {
                    fabScrollUp.show()
                } else if (!recyclerView.canScrollVertically(-1) && fabScrollUp.visibility == View.VISIBLE) {
                    fabScrollUp.hide()
                }
            }
        })
        
        fabScrollUp.setOnClickListener {
            recyclerView.smoothScrollToPosition(0)
        }

        loadProducts()
    }

    override fun onResume() {
        super.onResume()
        updateHeader()
        fetchUnreadCount()
    }

    private fun updateHeader() {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val role = prefs.getString("role", "Buyer") ?: "Buyer"
        val farmName = prefs.getString("farm_name", "")
        val fullName = prefs.getString("full_name", "")
        
        val tvHomeTitle = view?.findViewById<TextView>(R.id.tvHomeTitle) ?: return
        val llSearchContainer = view?.findViewById<View>(R.id.llSearchContainer) ?: return
        
        val name = if (!fullName.isNullOrEmpty()) fullName.split(" ")[0] else "User"

        if (role == "Farmer") {
            val displayFarm = if (farmName.isNullOrBlank()) "$name's Farm" else farmName
            tvHomeTitle.text = displayFarm
            llSearchContainer.visibility = View.GONE
        } else if (role == "Admin") {
            tvHomeTitle.text = "System Admin"
            llSearchContainer.visibility = View.VISIBLE
        } else {
            tvHomeTitle.text = name
            llSearchContainer.visibility = View.VISIBLE
        }
    }

    private fun setupChips(view: View) {
        val chipIds = listOf(R.id.chipAll, R.id.chipVegetables, R.id.chipFruits, R.id.chipGrains, R.id.chipLivestock, R.id.chipDairy, R.id.chipFodder, R.id.chipOthers)
        val chipGroup = mutableListOf<Chip>()
        
        chipIds.forEach { id ->
            view.findViewById<Chip>(id)?.let { chip ->
                chipGroup.add(chip)
                
                // Styling
                if (chip.id == R.id.chipAll) {
                    chip.setChipBackgroundColorResource(R.color.primary_dark)
                    chip.setTextColor(resources.getColor(R.color.white))
                }

                chip.setOnClickListener {
                    val category = if (chip.text == "All") null else chip.text.toString()
                    filterByCategory(category)
                    
                    chipGroup.forEach { 
                        it.setChipBackgroundColorResource(R.color.white)
                        it.setTextColor(resources.getColor(R.color.primary_dark))
                    }
                    chip.setChipBackgroundColorResource(R.color.primary_dark)
                    chip.setTextColor(resources.getColor(R.color.white))
                }
            }
        }
    }

    private fun filterByCategory(category: String?) {
        val filtered = if (category == null) {
            allProducts 
        } else {
            allProducts.filter { it.category.equals(category, ignoreCase = true) }
        }
        updateAdapter(filtered)
    }

    private fun fetchUnreadCount() {
        RetrofitClient.instance.getMessageCount().enqueue(object : Callback<Map<String, Int>> {
            override fun onResponse(call: Call<Map<String, Int>>, response: Response<Map<String, Int>>) {
                if (response.isSuccessful) {
                    val count = response.body()?.get("unread_count") ?: 0
                    if (count > 0) {
                        tvUnreadCount.text = count.toString()
                        tvUnreadCount.visibility = View.VISIBLE
                    } else {
                        tvUnreadCount.visibility = View.GONE
                    }
                }
            }
            override fun onFailure(call: Call<Map<String, Int>>, t: Throwable) {}
        })
    }

    private fun loadProducts(search: String? = null) {
        val swipeRefresh = view?.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh?.isRefreshing = true
        
        RetrofitClient.instance.getProducts(search).enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                swipeRefresh?.isRefreshing = false
                if (response.isSuccessful) {
                    allProducts = response.body() ?: emptyList()
                    updateAdapter(allProducts)
                    
                    val llEmpty = view?.findViewById<View>(R.id.llEmptyState)
                    if (allProducts.isEmpty()) {
                        llEmpty?.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        llEmpty?.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                }
            }
            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                swipeRefresh?.isRefreshing = false
                Snackbar.make(recyclerView, "Network error", Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateAdapter(list: List<Product>) {
        recyclerView.adapter = ProductAdapter(list.toMutableList(), 
            onEdit = { /* Edit logic */ },
            onDelete = { product -> deleteProduct(product) }
        )
    }

    private fun deleteProduct(product: Product) {
        val id = product.id ?: return
        RetrofitClient.instance.deleteProduct(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    loadProducts(etSearch.text.toString())
                    Snackbar.make(recyclerView, "Product removed", Snackbar.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {}
        })
    }
}
