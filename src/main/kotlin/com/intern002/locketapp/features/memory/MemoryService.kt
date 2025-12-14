package com.intern002.locketapp.features.memory

import java.util.*

class MemoryService(
    private val memoryRepository: MemoryRepository
) {

    suspend fun getMemories(userId: UUID, month: Int, year: Int): List<MemoryResponse> {
        return memoryRepository.getMemoriesByMonth(userId, month, year)
    }

    suspend fun createOrUpdateMemory(userId: UUID, request: CreateMemoryRequest): Boolean {
        if (request.postId.isBlank() || request.mediaUrl.isBlank()) {
            throw IllegalArgumentException("Data cannot be empty")
        }
        return memoryRepository.upsertMemory(userId, request)
    }
}