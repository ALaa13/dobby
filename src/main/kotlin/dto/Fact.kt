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
    @SerialName("confidence_score")
    val confidenceScore: Short?,
    @SerialName("roastability_score")
    val roastabilityScore: Short?,
    @SerialName("created_at")
    val createdAt: String
)
