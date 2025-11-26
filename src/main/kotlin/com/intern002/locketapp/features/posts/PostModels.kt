package com.intern002.locketapp.features.posts

import kotlinx.serialization.Serializable

@Serializable
data class CreatePostRequest(
    val mediaUrl: String,
    val mediaType: String,
    val caption: String? = null
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