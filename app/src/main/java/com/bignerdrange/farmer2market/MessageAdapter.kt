package com.bignerdrange.farmer2market

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.media.MediaPlayer
import com.google.android.material.button.MaterialButton
import com.squareup.picasso.Picasso

class MessageAdapter(private val messages: List<Message>, private val currentUserId: Int) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null
    private var playingUrl: String? = null

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val llSent: LinearLayout = view.findViewById(R.id.llSentMessage)
        val tvSentText: TextView = view.findViewById(R.id.tvSentText)
        val tvSentTime: TextView = view.findViewById(R.id.tvSentTime)
        val btnSentVoice: MaterialButton = view.findViewById(R.id.btnSentVoice)
        val ivReadStatus: ImageView = view.findViewById(R.id.ivReadStatus)

        val llReceived: LinearLayout = view.findViewById(R.id.llReceivedMessage)
        val tvReceivedText: TextView = view.findViewById(R.id.tvReceivedText)
        val tvReceivedTime: TextView = view.findViewById(R.id.tvReceivedTime)
        val btnReceivedVoice: MaterialButton = view.findViewById(R.id.btnReceivedVoice)
        val ivAvatar: ImageView = view.findViewById(R.id.ivMsgAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val msg = messages[position]
        val isSent = msg.sender == currentUserId

        if (isSent) {
            holder.llSent.visibility = View.VISIBLE
            holder.llReceived.visibility = View.GONE
            bindMessage(msg, holder.tvSentText, holder.tvSentTime, holder.btnSentVoice)
            
            if (msg.is_read) {
                holder.ivReadStatus.setColorFilter(androidx.core.content.ContextCompat.getColor(holder.itemView.context, R.color.primary))
            } else {
                holder.ivReadStatus.setColorFilter(androidx.core.content.ContextCompat.getColor(holder.itemView.context, R.color.text_hint))
            }
        } else {
            holder.llSent.visibility = View.GONE
            holder.llReceived.visibility = View.VISIBLE
            bindMessage(msg, holder.tvReceivedText, holder.tvReceivedTime, holder.btnReceivedVoice)
            
            // Avatar
            val avatar = msg.sender_details?.profile_picture
            if (!avatar.isNullOrEmpty()) {
                val fullUrl = if (avatar.startsWith("http")) avatar else "${RetrofitClient.BASE_URL.dropLast(1)}$avatar"
                Picasso.get().load(fullUrl).placeholder(R.drawable.ic_person).into(holder.ivAvatar)
            } else {
                holder.ivAvatar.setImageResource(R.drawable.ic_person)
            }
        }
    }

    private fun bindMessage(msg: Message, tvText: TextView, tvTime: TextView, btnVoice: MaterialButton) {
        if (msg.voice_note != null) {
            tvText.visibility = View.GONE
            btnVoice.visibility = View.VISIBLE
            btnVoice.setOnClickListener { handleVoicePlay(msg.voice_note, btnVoice) }
        } else {
            tvText.visibility = View.VISIBLE
            btnVoice.visibility = View.GONE
            tvText.text = msg.text
        }
        tvTime.text = formatTime(msg.timestamp)
    }

    private fun handleVoicePlay(url: String, button: MaterialButton) {
        if (playingUrl == url && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            playingUrl = null
            button.setIconResource(android.R.drawable.ic_media_play)
        } else {
            try {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    val fullUrl = if (url.startsWith("http")) url else "${RetrofitClient.BASE_URL.dropLast(1)}$url"
                    setDataSource(fullUrl)
                    prepareAsync()
                    setOnPreparedListener { 
                        start()
                        button.setIconResource(android.R.drawable.ic_media_pause)
                    }
                    setOnCompletionListener {
                        button.setIconResource(android.R.drawable.ic_media_play)
                        playingUrl = null
                    }
                }
                playingUrl = url
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun formatTime(timestamp: String?): String {
        return try {
            // "2024-04-25T12:45:00Z" -> "12:45 PM"
            val time = timestamp?.split("T")?.last()?.substring(0, 5) ?: ""
            time
        } catch (e: Exception) {
            ""
        }
    }

    override fun getItemCount() = messages.size
}
