package org.example.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Fact(
    @SerialName("id")
    val id: String,
    @SerialName("profile_id")
    val profileId: String,
    @SerialName("fact_text")
    val factText: String,
    @SerialName("source")
    val source: String?,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String? = null,
)
