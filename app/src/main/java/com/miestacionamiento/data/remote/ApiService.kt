package com.miestacionamiento.data.remote

import com.miestacionamiento.data.model.AuthResponse
import com.miestacionamiento.data.model.BookingResponse
import com.miestacionamiento.data.model.Conversation
import com.miestacionamiento.data.model.CreateBookingRequest
import com.miestacionamiento.data.model.CreateMessageRequest
import com.miestacionamiento.data.model.CreateParkingRequest
import com.miestacionamiento.data.model.CreateReviewRequest
import com.miestacionamiento.data.model.DeleteResponse
import com.miestacionamiento.data.model.LoginRequest
import com.miestacionamiento.data.model.Message
import com.miestacionamiento.data.model.OwnerDashboardData
import com.miestacionamiento.data.model.OwnerParking
import com.miestacionamiento.data.model.OwnerStats
import com.miestacionamiento.data.model.Parking
import com.miestacionamiento.data.model.RegisterRequest
import com.miestacionamiento.data.model.Review
import com.miestacionamiento.data.model.ReviewResponseRequest
import com.miestacionamiento.data.model.SaveResponse
import com.miestacionamiento.data.model.StartConversationRequest
import com.miestacionamiento.data.model.StartConversationResponse
import com.miestacionamiento.data.model.UserProfile
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // --- Autenticacion ---

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    // --- Estacionamientos ---

    @GET("parkings")
    suspend fun getParkings(
        @Header("X-User-Id") userId: Int? = null
    ): Response<List<Parking>>

    @GET("parkings/{id}")
    suspend fun getParkingById(
        @Path("id") id: Int,
        @Header("X-User-Id") userId: Int? = null
    ): Response<Parking>

    @GET("parkings/search")
    suspend fun searchParkings(
        @Query("q") query: String
    ): Response<List<Parking>>

    @GET("parkings/saved")
    suspend fun getSavedParkings(): Response<List<Parking>>

    @GET("parkings/recent")
    suspend fun getRecentParkings(): Response<List<Parking>>

    @POST("parkings/{id}/save")
    suspend fun toggleSave(@Path("id") id: Int): Response<SaveResponse>

    @POST("parkings/{id}/view")
    suspend fun markAsViewed(@Path("id") id: Int): Response<Void>

    // --- Perfil de usuario ---

    @GET("users/profile")
    suspend fun getProfile(): Response<UserProfile>

    @PUT("users/profile")
    suspend fun updateProfile(@Body profile: Map<String, String?>): Response<UserProfile>

    // --- Propietario: gestion de estacionamientos ---

    @GET("owner/parkings")
    suspend fun getOwnerParkings(): Response<List<OwnerParking>>

    @POST("owner/parkings")
    suspend fun createParking(@Body request: CreateParkingRequest): Response<OwnerParking>

    @PUT("owner/parkings/{id}")
    suspend fun updateParking(
        @Path("id") id: Int,
        @Body request: CreateParkingRequest
    ): Response<OwnerParking>

    @DELETE("owner/parkings/{id}")
    suspend fun deleteParking(@Path("id") id: Int): Response<DeleteResponse>

    @PATCH("owner/parkings/{id}/status")
    suspend fun toggleParkingStatus(@Path("id") id: Int): Response<OwnerParking>

    @GET("owner/stats")
    suspend fun getOwnerStats(): Response<OwnerStats>

    @GET("owner/dashboard")
    suspend fun getOwnerDashboard(): Response<OwnerDashboardData>

    // --- Reservas (Conductor) ---

    @POST("bookings")
    suspend fun createBooking(@Body request: CreateBookingRequest): Response<BookingResponse>

    // --- Reseñas ---

    @GET("reviews/parking/{parkingId}")
    suspend fun getReviews(@Path("parkingId") parkingId: Int): Response<List<Review>>

    @POST("reviews")
    suspend fun createReview(@Body request: CreateReviewRequest): Response<Review>

    @PUT("reviews/{reviewId}/response")
    suspend fun respondToReview(
        @Path("reviewId") reviewId: Int,
        @Body request: ReviewResponseRequest
    ): Response<Map<String, String>>

    @GET("reviews/my-parkings")
    suspend fun getOwnerReviews(): Response<List<Review>>

    // --- Chat ---

    @GET("chat/conversations")
    suspend fun getConversations(): Response<List<Conversation>>

    @POST("chat/conversations")
    suspend fun startConversation(@Body request: StartConversationRequest): Response<StartConversationResponse>

    @GET("chat/conversations/{conversationId}/messages")
    suspend fun getMessages(@Path("conversationId") conversationId: Int): Response<List<Message>>

    @POST("chat/conversations/{conversationId}/messages")
    suspend fun sendMessage(
        @Path("conversationId") conversationId: Int,
        @Body request: CreateMessageRequest
    ): Response<Message>
}
