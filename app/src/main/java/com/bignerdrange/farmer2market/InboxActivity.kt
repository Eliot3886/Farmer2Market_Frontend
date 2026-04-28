package com.bignerdrange.farmer2market

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InboxActivity : AppCompatActivity() {

    private lateinit var rvInbox: RecyclerView
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbox)

        rvInbox = findViewById(R.id.rvInbox)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarInbox)
        toolbar.setNavigationOnClickListener { finish() }

        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        currentUserId = prefs.getInt("user_id", -1)

        rvInbox.layoutManager = LinearLayoutManager(this)
        
        loadConversations()
    }

    private fun loadConversations() {
        RetrofitClient.instance.getConversations().enqueue(object : Callback<List<Conversation>> {
            override fun onResponse(call: Call<List<Conversation>>, response: Response<List<Conversation>>) {
                if (response.isSuccessful) {
                    val conversations = response.body() ?: emptyList()
                    
                    val llEmptyInbox = findViewById<android.view.View>(R.id.llEmptyInbox)
                    if (conversations.isEmpty()) {
                        llEmptyInbox.visibility = android.view.View.VISIBLE
                        rvInbox.visibility = android.view.View.GONE
                    } else {
                        llEmptyInbox.visibility = android.view.View.GONE
                        rvInbox.visibility = android.view.View.VISIBLE
                    }
                    
                    val adapter = InboxAdapter(conversations.toMutableList(), currentUserId, { conv ->
                        val isBuyer = currentUserId == conv.buyer
                        val otherId = if (isBuyer) conv.farmer else conv.buyer
                        val otherName = if (isBuyer) conv.farmer_details.full_name else conv.buyer_details.full_name
                        
                        val intent = Intent(this@InboxActivity, ChatActivity::class.java).apply {
                            putExtra("conversation_id", conv.id)
                            putExtra("receiver_id", otherId)
                            putExtra("receiver_name", otherName)
                            putExtra("product_id", conv.product)
                        }
                        startActivity(intent)
                    }, { conv, position ->
                        confirmDelete(conv, position)
                    })
                    rvInbox.adapter = adapter
                    
                    val itemTouchHelper = androidx.recyclerview.widget.ItemTouchHelper(object : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT or androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
                        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false
                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                            val pos = viewHolder.adapterPosition
                            val conv = conversations[pos]
                            confirmDelete(conv, pos)
                        }
                    })
                    itemTouchHelper.attachToRecyclerView(rvInbox)
                }
            }
            override fun onFailure(call: Call<List<Conversation>>, t: Throwable) {
                Snackbar.make(rvInbox, "Connection Error", Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun confirmDelete(conv: Conversation, position: Int) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Conversation")
            .setMessage("Are you sure you want to permanently delete this conversation?")
            .setPositiveButton("Delete") { _, _ ->
                val id = conv.id ?: return@setPositiveButton
                RetrofitClient.instance.deleteConversation(id).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            (rvInbox.adapter as? InboxAdapter)?.removeItem(position)
                            Snackbar.make(rvInbox, "Conversation deleted", Snackbar.LENGTH_SHORT).show()
                            
                            if (rvInbox.adapter?.itemCount == 0) {
                                findViewById<android.view.View>(R.id.llEmptyInbox).visibility = android.view.View.VISIBLE
                                rvInbox.visibility = android.view.View.GONE
                            }
                        }
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        (rvInbox.adapter as? InboxAdapter)?.notifyItemChanged(position)
                        Snackbar.make(rvInbox, "Failed to delete", Snackbar.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancel") { _, _ ->
                (rvInbox.adapter as? InboxAdapter)?.notifyItemChanged(position)
            }
            .setOnCancelListener {
                (rvInbox.adapter as? InboxAdapter)?.notifyItemChanged(position)
            }
            .show()
    }
}
