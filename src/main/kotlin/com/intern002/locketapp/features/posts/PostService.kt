package com.intern002.locketapp.features.posts

import com.intern002.locketapp.features.auth.AuthRepository
import java.util.*


class InvalidPostDataException(message: String) : Exception(message)
class PostCreationException : Exception("Could not create post")

class PostService(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository
) {

    suspend fun createPost(userId: UUID, request: CreatePostRequest): PostResponse {

        if (request.mediaType != "photo" && request.mediaType != "video") {
            throw InvalidPostDataException("Media type must be 'photo' or 'video'")
        }
        if (request.mediaUrl.isBlank()) {
            throw InvalidPostDataException("Media URL cannot be empty")
        }
        if (request.recipientIds.isEmpty()) {
            throw InvalidPostDataException("You must select at least one friend to send.")
        }

        val user = authRepository.findById(userId)
            ?: throw Exception("User not found")

        // 2. Gọi Repo
        val post = postRepository.createPost(userId, request)
            ?: throw PostCreationException()

        // 3. Map sang Response
        return PostResponse(
            id = post.id.toString(),
            authorId = post.authorId.toString(),
            mediaUrl = post.mediaUrl,
            authorName = user.username,
            authorAvatar = user.avatarUrl,
            mediaType = post.mediaType,
            caption = post.caption,
            createdAt = post.createdAt,
            reactionCount = 0,
            latestReactions = emptyList()
        )
    }

    suspend fun getPosts(userId: UUID, page: Int, pageSize: Int): List<PostResponse> {
        val validPage = if (page < 1) 1 else page
        val validSize = if (pageSize < 1) 20 else pageSize

        val posts = postRepository.getPosts(userId, validPage, validSize)

        return postRepository.getPosts(userId, validPage, validSize)
    }
}