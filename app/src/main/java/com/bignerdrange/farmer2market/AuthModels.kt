package com.bignerdrange.farmer2market

data class AuthResponse(
    val access: String,
    val refresh: String,
    val user: UserTokenData
)

data class UserTokenData(
    val id: Int,
    val username: String,
    val full_name: String,
    val phone_number: String,
    val location: String?,
    val role: String,
    val farm_name: String?,
    val profile_picture: String?,
    val email: String?,
    val date_joined: String?
)

data class TokenResponse(
    val access: String
)

data class AdminDashboardData(
    val stats: AdminStats,
    val users: List<UserTokenData>
)

data class AdminStats(
    val farmers: Int,
    val buyers: Int,
    val products: Int
)
