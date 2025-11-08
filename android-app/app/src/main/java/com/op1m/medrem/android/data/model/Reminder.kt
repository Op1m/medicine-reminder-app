package com.op1m.medrem.android.data.model

import com.google.gson.annotations.SerializedName

data class Reminder(
    val id: Long? = null,
    val user: User? = null,
    val medicine: Medicine? = null,

    @SerializedName("reminder_time")
    val reminderTime: String,

    @SerializedName("is_active")
    val isActive: Boolean = true,

    @SerializedName("days_of_week")
    val daysOfWeek: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class ReminderCreateRequest(
    @SerializedName("user_id")
    val userId: Long,

    @SerializedName("medicine_id")
    val medicineId: Long,

    @SerializedName("reminder_time")
    val reminderTime: String,

    @SerializedName("days_of_week")
    val daysOfWeek: String? = null
)