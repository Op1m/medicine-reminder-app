package com.op1m.medrem.android.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Long? = null,
    val username: String,
    val email: String,

    @SerializedName("first_name")
    val firstName: String? = null,

    @SerializedName("last_name")
    val lastName: String? = null,

    @SerializedName("telegram_chat_id")
    val telegramChatId: Long? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null,

    @SerializedName("is_active")
    val isActive: Boolean = true
)

data class UserRegistrationRequest(
    val username: String,
    val password: String,
    val email: String,
    val firstName: String? = null
)

data class UserUpdateRequest(
    val username: String,
    val email: String,
    val firstName: String?,
    val lastName: String?
)

data class PasswordChangeRequest(
    val oldPassword: String,
    val newPassword: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class AuthResponse(
    val user: User,
    val token: String? = null
)