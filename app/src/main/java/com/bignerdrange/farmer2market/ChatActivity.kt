package com.bignerdrange.farmer2market

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatActivity : AppCompatActivity() {

    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: MaterialButton
    private var messages = mutableListOf<Message>()
    private var productId: Int = -1
    private var receiverId: Int = -1
    private var currentUserId: Int = -1
    private var isRecording = false
    private var mediaRecorder: android.media.MediaRecorder? = null
    private var audioFile: java.io.File? = null

    private lateinit var wsManager: WebSocketManager
    private var conversationId: Int? = null
    private var adapter: MessageAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        rvChat = findViewById(R.id.rvChat)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarChat)

        toolbar.setNavigationOnClickListener { finish() }

        productId = intent.getIntExtra("product_id", -1)
        receiverId = intent.getIntExtra("receiver_id", -1)
        val receiverName = intent.getStringExtra("receiver_name") ?: "Chat"
        toolbar.title = receiverName

        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        currentUserId = prefs.getInt("user_id", -1)

        rvChat.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        
        conversationId = intent.getIntExtra("conversation_id", -1).takeIf { it != -1 }
        
        // Use a unique room name for the conversation or (product, buyer, farmer) triplet
        val roomName = if (conversationId != null) "conv_$conversationId" else {
            // Create a deterministic room name if no conversation exists yet
            val ids = listOf(currentUserId, receiverId).sorted()
            "prod_${productId}_${ids[0]}_${ids[1]}"
        }

        wsManager = WebSocketManager(roomName, { text ->
            runOnUiThread {
                val jsonObj = org.json.JSONObject(text)
                if (jsonObj.getString("type") == "chat_message") {
                    if (conversationId == null) {
                        val msgJson = jsonObj.optJSONObject("message")
                        if (msgJson != null && msgJson.has("conversation") && !msgJson.isNull("conversation")) {
                            conversationId = msgJson.getInt("conversation")
                        }
                    }
                    loadMessages()
                }
            }
        }, { id, isTyping ->
            runOnUiThread {
                if (id == receiverId) {
                    toolbar.subtitle = if (isTyping) "typing..." else ""
                }
            }
        })
        wsManager.connect()

        loadMessages()

        etMessage.addTextChangedListener(object: android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                if(s.isNullOrEmpty()) {
                    btnSend.setIconResource(android.R.drawable.ic_btn_speak_now)
                } else {
                    btnSend.setIconResource(android.R.drawable.ic_menu_send)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnSend.setOnTouchListener { v, event ->
            val isTextEmpty = etMessage.text.toString().trim().isEmpty()
            
            if (!isTextEmpty) {
                if (event.action == android.view.MotionEvent.ACTION_UP) {
                    v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                    sendMessage(etMessage.text.toString().trim())
                }
                return@setOnTouchListener false
            }
            
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 200)
                    } else {
                        startRecording()
                    }
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    if (isRecording) {
                        stopRecordingAndSend()
                    }
                }
            }
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        wsManager.disconnect()
    }

    private fun loadMessages() {
        val call = if (conversationId != null) {
            RetrofitClient.instance.getConversationHistory(conversationId!!)
        } else {
            RetrofitClient.instance.getMessages()
        }

        call.enqueue(object : Callback<List<Message>> {
            override fun onResponse(call: Call<List<Message>>, response: Response<List<Message>>) {
                if (response.isSuccessful) {
                    val allMsg = response.body() ?: emptyList()
                    val newMessages = if (conversationId != null) {
                        allMsg
                    } else {
                        allMsg.filter { 
                            (it.sender == currentUserId && it.receiver == receiverId) ||
                            (it.sender == receiverId && it.receiver == currentUserId)
                        }
                    }
                    
                    if (newMessages.size != messages.size) {
                        messages = newMessages.toMutableList()
                        adapter = MessageAdapter(messages, currentUserId)
                        rvChat.adapter = adapter
                        rvChat.smoothScrollToPosition(messages.size - 1)
                    }

                    if (conversationId != null) {
                        RetrofitClient.instance.markConversationAsRead(conversationId!!).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {}
                            override fun onFailure(call: Call<Void>, t: Throwable) {}
                        })
                    }
                }
            }
            override fun onFailure(call: Call<List<Message>>, t: Throwable) {}
        })
    }

    private fun sendMessage(text: String) {
        wsManager.sendMessage(currentUserId, receiverId, productId, text, conversationId)
        etMessage.text.clear()
        
        val optimisticMsg = Message(
            sender = currentUserId,
            receiver = receiverId,
            product = if (productId != -1) productId else null,
            text = text,
            timestamp = "TJust now",
            status = "sent"
        )
        messages.add(optimisticMsg)
        if (adapter == null) {
            adapter = MessageAdapter(messages, currentUserId)
            rvChat.adapter = adapter
        } else {
            adapter?.notifyItemInserted(messages.size - 1)
        }
        rvChat.smoothScrollToPosition(messages.size - 1)
    }

    private fun startRecording() {
        try {
            audioFile = java.io.File(cacheDir, "voice_note_${System.currentTimeMillis()}.mp4")
            mediaRecorder = android.media.MediaRecorder().apply {
                setAudioSource(android.media.MediaRecorder.AudioSource.MIC)
                setOutputFormat(android.media.MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }
            isRecording = true
            etMessage.hint = "Recording..."
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRecordingAndSend() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            etMessage.hint = "Type a message..."
            
            if (audioFile != null && audioFile!!.exists()) {
                sendVoiceMessage(audioFile!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendVoiceMessage(file: java.io.File) {
        val reqFile = file.asRequestBody("audio/mp4".toMediaType())
        val body = MultipartBody.Part.createFormData("voice_note", file.name, reqFile)
        val rReceiver = receiverId.toString().toRequestBody(MultipartBody.FORM)
        val rProduct = if (productId != -1) productId.toString().toRequestBody(MultipartBody.FORM) else null
        
        RetrofitClient.instance.sendMessageMultipart(rReceiver, rProduct, null, body).enqueue(object : Callback<Message> {
            override fun onResponse(call: Call<Message>, response: Response<Message>) {
                if(response.isSuccessful){
                    loadMessages()
                }
            }
            override fun onFailure(call: Call<Message>, t: Throwable) {}
        })
    }
}
