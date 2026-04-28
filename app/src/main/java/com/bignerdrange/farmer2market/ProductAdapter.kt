package com.bignerdrange.farmer2market

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.squareup.picasso.Picasso

class ProductAdapter(
    private var list: MutableList<Product>,
    private val onEdit: (Product) -> Unit,
    private val onDelete: (Product) -> Unit,
    private val isHistory: Boolean = false
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    private var currentUserId: Int = -1
    private var currentUserRole: String = "Buyer"

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvName)
        val price: TextView = view.findViewById(R.id.tvPrice)
        val image: ImageView = view.findViewById(R.id.ivProduct)
        val btnEdit: ImageButton = view.findViewById(R.id.ivEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.ivDelete)
        val tvFarmName: TextView = view.findViewById(R.id.tvFarmName)
        val tvContact: TextView = view.findViewById(R.id.tvContact)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val llActions: View = view.findViewById(R.id.llActions)
        val tvYourProduct: TextView = view.findViewById(R.id.tvYourProduct)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val prefs = parent.context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        currentUserId = prefs.getInt("user_id", -1)
        currentUserRole = prefs.getString("role", "Buyer") ?: "Buyer"

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = list[position]
        holder.name.text = product.name
        
        try {
            val priceVal = product.price.toDouble()
            val format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US)
            holder.price.text = format.format(priceVal)
        } catch (e: Exception) {
            holder.price.text = product.price // REMOVED "$" HERE
        }

        val farmName = product.owner_details?.farm_name ?: product.owner_details?.full_name ?: "Farmer"
        holder.tvFarmName.text = farmName
        
        if (product.owner_details?.id == currentUserId) {
            holder.tvYourProduct.visibility = View.VISIBLE
        } else {
            holder.tvYourProduct.visibility = View.GONE
        }
        
        // Apply thin green border
        holder.itemView.setBackgroundResource(R.drawable.card_green_border)

        holder.tvContact.text = product.owner_details?.phone_number ?: product.contact ?: "N/A"
        holder.tvLocation.text = product.location ?: "N/A"

        // Show actions ONLY on history page or for Admin
        if (isHistory && (product.owner_details?.id == currentUserId || currentUserRole == "Admin")) {
            holder.llActions.visibility = View.VISIBLE
        } else {
            holder.llActions.visibility = View.GONE
        }

        // Marketplace constraints: Show farm name, contact and location
        holder.tvFarmName.visibility = View.VISIBLE
        holder.tvContact.visibility = View.VISIBLE
        holder.tvLocation.visibility = View.VISIBLE

        var imageUrl = product.image
        if (!imageUrl.isNullOrEmpty()) {
            if (!imageUrl.startsWith("http")) {
                imageUrl = "${RetrofitClient.BASE_URL.dropLast(1)}$imageUrl"
            }
            Picasso.get()
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(holder.image)
        } else {
            holder.image.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.btnEdit.setOnClickListener { onEdit(product) }
        
        holder.btnDelete.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete Product")
                .setMessage("Do you want to delete the produce?")
                .setPositiveButton("Delete") { _, _ ->
                    onDelete(product)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }


        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ProductDetailActivity::class.java).apply {
                putExtra("name", product.name)
                putExtra("price", product.price)
                putExtra("location", product.location)
                putExtra("category", product.category)
                putExtra("farm", farmName)
                // Use registration phone number
                putExtra("phone", product.owner_details?.phone_number)
                putExtra("image", product.image)
                putExtra("product_id", product.id)
                putExtra("owner_id", product.owner_details?.id)
                putExtra("owner_name", product.owner_details?.username)
                putExtra("owner_joined", product.owner_details?.date_joined)
                putExtra("description", product.description)
                putExtra("views", product.views)
                putExtra("created_at", product.created_at)
            }
            context.startActivity(intent)
        }
    }
    
    fun updateData(newList: List<Product>) {
        list = newList.toMutableList()
        notifyDataSetChanged()
    }
}
