package com.intern002.locketapp.features.reaction

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReactionTypeResponse(
    val id: Int,
    val name: String?,
    val emoji: String?,
    @SerialName("image_url")
    val imageUrl: String?
)

@Serializable
data class ReactToPostRequest(
    @SerialName("post_id")
    val postId: String,

    @SerialName("reaction_type_id")
    val reactionTypeId: Int
)