package com.bignerdrange.farmer2market

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class InboxAdapter(
    private val conversations: MutableList<Conversation>,
    private val currentUserId: Int,
    private val onClick: (Conversation) -> Unit,
    private val onLongClick: (Conversation, Int) -> Unit
) : RecyclerView.Adapter<InboxAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvChatName)
        val lastMsg: TextView = view.findViewById(R.id.tvLastMsg)
        val time: TextView = view.findViewById(R.id.tvChatTime)
        val avatar: ImageView = view.findViewById(R.id.ivChatAvatar)
        val unread: TextView = view.findViewById(R.id.tvUnreadCount)
        val productName: TextView = view.findViewById(R.id.tvChatProductName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inbox, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conv = conversations[position]
        val isBuyer = currentUserId == conv.buyer
        val otherUser = if (isBuyer) conv.farmer_details else conv.buyer_details
        
        holder.name.text = otherUser.full_name
        holder.productName.text = "Product: ${conv.product_details.name}"
        holder.lastMsg.text = conv.last_message?.text ?: "Voice Note"
        holder.time.text = formatTime(conv.updated_at)
        
        if (conv.unread_count > 0) {
            holder.unread.visibility = View.VISIBLE
            holder.unread.text = conv.unread_count.toString()
            holder.lastMsg.setTypeface(null, android.graphics.Typeface.BOLD)
            holder.lastMsg.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.context, R.color.text_primary))
            holder.time.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.context, R.color.primary))
        } else {
            holder.unread.visibility = View.GONE
            holder.lastMsg.setTypeface(null, android.graphics.Typeface.NORMAL)
            holder.lastMsg.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.context, R.color.text_secondary))
            holder.time.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.context, R.color.text_hint))
        }

        val avatarUrl = otherUser.profile_picture
        if (!avatarUrl.isNullOrEmpty()) {
            val fullUrl = if (avatarUrl.startsWith("http")) avatarUrl else "${RetrofitClient.BASE_URL.dropLast(1)}$avatarUrl"
            Picasso.get().load(fullUrl).placeholder(R.drawable.ic_person).into(holder.avatar)
        } else {
            holder.avatar.setImageResource(R.drawable.ic_person)
        }
        
        holder.itemView.setOnClickListener { onClick(conv) }
        holder.itemView.setOnLongClickListener {
            onLongClick(conv, position)
            true
        }
    }
    
    fun removeItem(position: Int) {
        conversations.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun formatTime(timestamp: String): String {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val date = sdf.parse(timestamp) ?: return timestamp
            
            val calendar = java.util.Calendar.getInstance()
            val today = calendar.get(java.util.Calendar.DAY_OF_YEAR)
            val todayYear = calendar.get(java.util.Calendar.YEAR)
            
            calendar.time = date
            val msgDay = calendar.get(java.util.Calendar.DAY_OF_YEAR)
            val msgYear = calendar.get(java.util.Calendar.YEAR)
            
            val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            val timeStr = timeFormat.format(date)
            
            if (todayYear == msgYear) {
                if (today == msgDay) return "Today $timeStr"
                if (today - 1 == msgDay) return "Yesterday $timeStr"
            }
            
            val dateFormat = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
            dateFormat.format(date)
        } catch (e: Exception) {
            timestamp.split("T").lastOrNull()?.substring(0, 5) ?: timestamp
        }
    }

    override fun getItemCount() = conversations.size
}
