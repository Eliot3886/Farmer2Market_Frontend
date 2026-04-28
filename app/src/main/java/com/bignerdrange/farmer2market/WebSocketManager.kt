package com.bignerdrange.farmer2market

import android.util.Log
import okhttp3.*
import okio.ByteString

class WebSocketManager(
    private val roomName: String,
    private val onMessageReceived: (String) -> Unit,
    private val onTyping: (Int, Boolean) -> Unit
) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    fun connect() {
        // Replace with your local IP and ensure /ws/chat/ prefix
        val wsUrl = "${RetrofitClient.BASE_URL.replace("http://", "ws://")}ws/chat/$roomName/"
        val request = Request.Builder().url(wsUrl).build()
        
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connected to $wsUrl")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                onMessageReceived(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Error: ${t.message}")
                // Simple retry
                // connect() 
            }
        })
    }

    fun sendMessage(senderId: Int, receiverId: Int, productId: Int, message: String, conversationId: Int? = null) {
        val json = """
            {
                "type": "chat_message",
                "message": "$message",
                "sender_id": $senderId,
                "receiver_id": $receiverId,
                "product_id": $productId,
                "conversation_id": ${conversationId ?: "null"}
            }
        """.trimIndent()
        webSocket?.send(json)
    }

    fun sendTyping(senderId: Int, isTyping: Boolean) {
        val json = """
            {
                "type": "typing",
                "sender_id": $senderId,
                "is_typing": $isTyping
            }
        """.trimIndent()
        webSocket?.send(json)
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
    }
}
