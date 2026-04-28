package com.bignerdrange.farmer2market

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val image = findViewById<ImageView>(R.id.ivDetailImage)
        val name = findViewById<TextView>(R.id.tvDetailName)
        val price = findViewById<TextView>(R.id.tvDetailPrice)
        val timeSent = findViewById<TextView>(R.id.tvTimeSent)
        val location = findViewById<TextView>(R.id.tvDetailLocation)
        val category = findViewById<TextView>(R.id.tvDetailCategory)
        val seller = findViewById<TextView>(R.id.tvDetailSeller)
        val btnContact = findViewById<MaterialButton>(R.id.btnContactSeller)
        val btnWhatsApp = findViewById<MaterialButton>(R.id.btnWhatsApp)
        val btnChat = findViewById<MaterialButton>(R.id.btnChat)
        val llLocation = findViewById<View>(R.id.llLocationAction)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val tvViews = findViewById<TextView>(R.id.tvDetailViews) ?: null
        val description = findViewById<TextView>(R.id.tvDetailDescription)

        toolbar.setNavigationOnClickListener { finish() }

        // Data from intent
        val pName = intent.getStringExtra("name")
        val pPrice = intent.getStringExtra("price")
        val pLoc = intent.getStringExtra("location")
        val pCat = intent.getStringExtra("category")
        val pFarm = intent.getStringExtra("farm")
        // Always prefer the registration phone number if available
        val pPhone = intent.getStringExtra("phone")
        val pImg = intent.getStringExtra("image")
        val pId = intent.getIntExtra("product_id", -1)
        val oId = intent.getIntExtra("owner_id", -1)
        val oName = intent.getStringExtra("owner_name") ?: "Farmer"
        val pTime = intent.getStringExtra("created_at")
        val pViews = intent.getIntExtra("views", 0)

        name.text = pName
        price.text = pPrice // No quantity here as requested
        location.text = pLoc
        category.text = pCat?.uppercase()
        val tvSellerJoined = findViewById<TextView>(R.id.tvSellerJoined)
        val llSellerSection = findViewById<View>(R.id.llSellerSection)

        seller.text = "Sold by $pFarm"
        tvViews?.text = pViews.toString()
        timeSent.text = formatTime(pTime)
        description.text = intent.getStringExtra("description") ?: "No description provided."
        
        // Joined date logic
        val joinedAt = intent.getStringExtra("owner_joined") ?: "2024"
        tvSellerJoined.text = "Joined in ${joinedAt.split("-").first()}"

        if (!pImg.isNullOrEmpty()) {
            var fullUrl = pImg
            if (!fullUrl.startsWith("http")) {
                fullUrl = "${RetrofitClient.BASE_URL.dropLast(1)}$fullUrl"
            }
            Picasso.get().load(fullUrl).into(image)
        }

        // Increment views on backend
        if (pId != -1) {
            RetrofitClient.instance.incrementProductView(pId).enqueue(object : Callback<Map<String, Int>> {
                override fun onResponse(call: Call<Map<String, Int>>, response: Response<Map<String, Int>>) {
                    if (response.isSuccessful) {
                        tvViews?.text = response.body()?.get("views")?.toString() ?: pViews.toString()
                    }
                }
                override fun onFailure(call: Call<Map<String, Int>>, t: Throwable) {}
            })
        }

        btnContact.setOnClickListener {
            pPhone?.let { phone ->
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phone")
                }
                startActivity(intent)
            }
        }

        btnWhatsApp.setOnClickListener {
            pPhone?.let { phone ->
                // Properly format number for international WhatsApp API if needed
                var cleanPhone = phone.replace(" ", "").replace("+", "")
                if (cleanPhone.startsWith("0")) {
                    cleanPhone = "263" + cleanPhone.drop(1)
                } else if (!cleanPhone.startsWith("263")) {
                    cleanPhone = "263" + cleanPhone
                }
                
                val msg = "Hello $pFarm, I'm interested in your $pName ($pPrice) listed on Farmer2Market."
                val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(msg)}"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }
        }

        btnChat.setOnClickListener {
            openChat(oId, pId, oName)
        }


        llSellerSection.setOnClickListener {
            showFarmerProfileDialog(oId, oName, pFarm, pPhone, joinedAt)
        }

        llLocation.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:0,0?q=$pLoc")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            try {
                startActivity(mapIntent)
            } catch (e: Exception) {
                Snackbar.make(llLocation, "Maps app not found", Snackbar.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageView>(R.id.ivShare)?.setOnClickListener {
            val shareText = "Check out $pName from $pFarm at $pPrice! pickup at $pLoc. Download Farmer2Market to buy."
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(sendIntent, null))
        }
    }

    private fun showFarmerProfileDialog(oId: Int, oName: String?, farmName: String?, phone: String?, joinedAt: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_farmer_profile, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        dialogView.findViewById<TextView>(R.id.tvDialogFarmName).text = farmName ?: "Farmer"
        dialogView.findViewById<TextView>(R.id.tvDialogFarmerName).text = oName ?: "Farmer"
        dialogView.findViewById<TextView>(R.id.tvDialogPhone).text = phone ?: "N/A"
        dialogView.findViewById<TextView>(R.id.tvDialogJoined).text = "Joined Farmer2Market in ${joinedAt.split("-").first()}"
        
        dialogView.findViewById<MaterialButton>(R.id.btnDialogCall).setOnClickListener {
            phone?.let { p ->
                startActivity(Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:$p") })
            }
        }
        
        dialog.show()
    }

    private fun shareProduct(pName: String?, pFarm: String?, pPrice: String?, pLoc: String?) {
        val shareText = "Check out $pName from $pFarm at $pPrice! pickup at $pLoc. Download Farmer2Market to buy."
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, null))
    }

    private fun openChat(oId: Int, pId: Int, oName: String?) {
        if (oId != -1) {
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("product_id", pId)
                putExtra("receiver_id", oId)
                putExtra("receiver_name", oName ?: "Farmer")
            }
            startActivity(intent)
        }
    }

    private fun formatTime(timestamp: String?): String {
        return if (timestamp.isNullOrEmpty()) "Recently posted" else try {
            "Posted: ${timestamp.split("T").first()}"
        } catch (e: Exception) {
            "Posted recently"
        }
    }
}
