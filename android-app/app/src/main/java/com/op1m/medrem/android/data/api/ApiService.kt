package com.op1m.medrem.android.data.api

import com.google.gson.annotations.SerializedName
import com.op1m.medrem.android.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("users/register")
    suspend fun register(@Body request: UserRegistrationRequest): Response<User>

    @GET("users/me")
    suspend fun getCurrentUser(): Response<User>


    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<User>

    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") id: Long,
        @Body request: UserUpdateRequest
    ): Response<User>

    @PATCH("users/{id}/password")
    suspend fun changePassword(
        @Path("id") id: Long,
        @Body request: PasswordChangeRequest
    ): Response<Unit>

    @POST("users/{userId}/link-telegram")
    suspend fun linkTelegram(
        @Path("userId") userId: Long,
        @Body request: TelegramLinkRequest
    ): Response<User>


    // @GET("medicines")
    // suspend fun getMedicines(): Response<List<Medicine>>

    // @GET("reminders/user/{userId}")
    // suspend fun getUserReminders(@Path("userId") userId: Long): Response<List<Reminder>>
}

data class TelegramLinkRequest(
    @SerializedName("telegramChatId")
    val telegramChatId: Long
)