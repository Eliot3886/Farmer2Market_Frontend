package com.bignerdrange.farmer2market

data class Conversation(
    val id: Int,
    val buyer: Int,
    val farmer: Int,
    val product: Int,
    val created_at: String,
    val updated_at: String,
    val buyer_details: UserTokenData,
    val farmer_details: UserTokenData,
    val product_details: Product,
    val last_message: Message?,
    val unread_count: Int
)
