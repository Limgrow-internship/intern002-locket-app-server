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
data class PostReactionDto(

    @SerialName("user_id")
    val userId: String,

    val username: String,

    @SerialName("avatar_url")
    val avatarUrl: String?,

    val emoji: String

)


@Serializable
data class PostResponse(
    val id: String,
    val authorId: String,
    val mediaUrl: String,
    @SerialName("author_name")
    val authorName: String,
    @SerialName("author_avatar")
    val authorAvatar: String?,
    val mediaType: String,
    val caption: String?,
    val createdAt: String,
    @SerialName("reaction_count")
    val reactionCount: Int?,
    @SerialName("latest_reactions")
    val latestReactions: List<PostReactionDto>?
)