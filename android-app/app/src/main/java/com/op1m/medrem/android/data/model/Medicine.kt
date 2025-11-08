package com.op1m.medrem.android.data.model

import com.google.gson.annotations.SerializedName

data class Medicine(
    val id: Long? = null,
    val name: String,
    val dosage: String? = null,
    val description: String? = null,
    val instructions: String? = null,

    @SerializedName("is_active")
    val isActive: Boolean = true,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class MedicineCreateRequest(
    val name: String,
    val dosage: String? = null,
    val description: String? = null,
    val instructions: String? = null
)