package com.intern002.locketapp.features.memory

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

data class Memory(
    val id: UUID,
    val postId: UUID,
    val authorId: UUID,
    val mediaUrl: String,
    val date: String,
    val createdAt: String
)

@Serializable
data class MemoryResponse(
    val id: String,

    @SerialName("post_id")
    val postId: String,

    @SerialName("author_id")
    val authorId: String,

    @SerialName("media_url")
    val mediaUrl: String,

    val date: String, // YYYY-MM-DD

    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class CreateMemoryRequest(
    @SerialName("post_id")
    val postId: String,

    @SerialName("media_url")
    val mediaUrl: String,

    val date: String
)