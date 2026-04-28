package com.bignerdrange.farmer2market

data class Product(
    val id: Int? = null,
    val name: String,
    val category: String,
    val price: String,
    val quantity: String,
    val location: String,
    val contact: String? = null,
    val description: String? = null,
    val created_at: String? = null,
    val image: String? = null,
    val views: Int = 0,
    val owner_details: UserTokenData? = null
)