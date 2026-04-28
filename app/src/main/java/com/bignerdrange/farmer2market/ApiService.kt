package com.bignerdrange.farmer2market

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("api/register/")
    fun register(@Body data: Map<String, String?>): Call<Void>

    @POST("api/login/")
    fun login(@Body data: Map<String, String>): Call<AuthResponse>

    @POST("api/reset-password/")
    fun resetPassword(@Body data: Map<String, String>): Call<Void>

    @POST("api/refresh/")
    fun refresh(@Body data: Map<String, String>): Call<TokenResponse>

    @GET("api/profile/")
    fun getProfile(): Call<UserTokenData>

    @PATCH("api/profile/")
    fun updateProfile(@Body data: Map<String, String?>): Call<UserTokenData>

    @Multipart
    @PATCH("api/profile/")
    fun updateProfilePicture(
        @Part profile_picture: MultipartBody.Part
    ): Call<UserTokenData>

    @DELETE("api/profile/")
    fun deleteProfile(): Call<Void>

    @POST("api/clear-db/")
    fun clearDatabase(): Call<Void>

    @GET("api/products/")
    fun getProducts(@Query("search") search: String? = null): Call<List<Product>>

    @GET("api/products/my/")
    fun getMyProducts(@Query("owner_id") ownerId: Int? = null): Call<List<Product>>

    @Multipart
    @POST("api/products/")
    fun addProduct(
        @Part("name") name: RequestBody,
        @Part("category") category: RequestBody,
        @Part("price") price: RequestBody,
        @Part("quantity") quantity: RequestBody,
        @Part("location") location: RequestBody,
        @Part("contact") contact: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Call<Product>

    @DELETE("api/products/{id}/")
    fun deleteProduct(@Path("id") id: Int): Call<Void>

    @Multipart
    @PATCH("api/products/{id}/")
    fun updateProduct(
        @Path("id") id: Int,
        @Part("name") name: RequestBody?,
        @Part("category") category: RequestBody?,
        @Part("price") price: RequestBody?,
        @Part("quantity") quantity: RequestBody?,
        @Part("location") location: RequestBody?,
        @Part("contact") contact: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Call<Product>

    @GET("api/messages/")
    fun getMessages(): Call<List<Message>>

    @POST("api/messages/")
    fun sendMessage(@Body data: Map<String, Any?>): Call<Message>

    @Multipart
    @POST("api/messages/")
    fun sendMessageMultipart(
        @Part("receiver") receiver: RequestBody,
        @Part("product") product: RequestBody?,
        @Part("text") text: RequestBody?,
        @Part voice_note: MultipartBody.Part?
    ): Call<Message>
    @GET("api/messages/count/")
    fun getMessageCount(): Call<Map<String, Int>>

    @GET("api/conversations/")
    fun getConversations(): Call<List<Conversation>>

    @GET("api/messages/history/")
    fun getConversationHistory(@Query("conversation_id") conversationId: Int): Call<List<Message>>

    @POST("api/conversations/{id}/read/")
    fun markConversationAsRead(@Path("id") id: Int): Call<Void>

    @DELETE("api/conversations/{id}/")
    fun deleteConversation(@Path("id") id: Int): Call<Void>

    @POST("api/products/{id}/view/")
    fun incrementProductView(@Path("id") id: Int): Call<Map<String, Int>>

    @GET("api/admin/dashboard/")
    fun getAdminDashboard(): Call<AdminDashboardData>

    @DELETE("api/admin/users/{id}/")
    fun adminDeleteUser(@Path("id") id: Int): Call<Void>
}
