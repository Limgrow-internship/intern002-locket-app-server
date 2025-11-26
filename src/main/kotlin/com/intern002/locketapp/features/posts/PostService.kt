package com.intern002.locketapp.features.posts

import java.util.*

class PostService(private val postRepository: PostRepository) {

    suspend fun createPost(userId: String, request: CreatePostRequest): PostResponse {

        if (request.mediaType != "photo" && request.mediaType != "video") {
            throw IllegalArgumentException("Media type must be 'photo' or 'video'")
        }

        val userUuid = UUID.fromString(userId)

        val post = postRepository.createPost(userUuid, request)
            ?: throw Exception("Failed to create post")
        
        return PostResponse(
            id = post.id.toString(),
            authorId = post.authorId.toString(),
            mediaUrl = post.mediaUrl,
            mediaType = post.mediaType,
            caption = post.caption,
            createdAt = post.createdAt
        )
    }
}