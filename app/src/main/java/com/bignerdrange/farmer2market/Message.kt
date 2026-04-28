package com.bignerdrange.farmer2market

data class Message(
    val id: Int? = null,
    val conversation: Int? = null,
    val sender: Int? = null,
    val receiver: Int? = null,
    val product: Int? = null,
    val text: String? = null,
    val voice_note: String? = null,
    val timestamp: String? = null,
    val is_read: Boolean = false,
    val status: String = "sent",
    val sender_details: UserTokenData? = null,
    val receiver_details: UserTokenData? = null
)
