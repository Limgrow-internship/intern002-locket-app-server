package com.intern002.locketapp.features.reaction

import java.util.*

class ReactionService(private val reactionRepository: ReactionRepository) {

    suspend fun getAllReactionTypes(): List<ReactionTypeResponse> {
        return reactionRepository.getReactionTypes()
    }

    suspend fun reactToPost(userId: UUID, request: ReactToPostRequest) {
        val postIdUuid = try {
            UUID.fromString(request.postId)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid Post ID format")
        }

        reactionRepository.reactToPost(userId, postIdUuid, request.reactionTypeId)
    }
}