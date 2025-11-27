package com.intern002.locketapp.features.posts

import java.util.*


class InvalidPostDataException(message: String) : Exception(message)
class PostCreationException : Exception("Could not create post")

class PostService(private val postRepository: PostRepository) {

    suspend fun createPost(userId: UUID, request: CreatePostRequest): PostResponse {

        if (request.mediaType != "photo" && request.mediaType != "video") {
            throw InvalidPostDataException("Media type must be 'photo' or 'video'")
        }
        if (request.mediaUrl.isBlank()) {
            throw InvalidPostDataException("Media URL cannot be empty")
        }

        // 2. Gọi Repo
        val post = postRepository.createPost(userId, request)
            ?: throw PostCreationException()

        // 3. Map sang Response
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