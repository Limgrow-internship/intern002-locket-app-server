package com.intern002.locketapp.features.posts

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreatePostRequest(
    @SerialName("media_url")
    val mediaUrl: String,
    @SerialName("media_type")
    val mediaType: String,

    @SerialName("caption")
    val caption: String?,

    @SerialName("recipient_ids")
    val recipientIds: List<String>
)

@Serializable
data class PostResponse(
    val id: String,
    val authorId: String,
    val mediaUrl: String,
    val mediaType: String,
    val caption: String?,
    val createdAt: String
)