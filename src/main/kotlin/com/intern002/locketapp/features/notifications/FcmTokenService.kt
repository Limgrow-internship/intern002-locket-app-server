package com.intern002.locketapp.features.notifications

import java.util.UUID

class FcmTokenService(private val repository: FcmTokenRepository) {

    suspend fun registerToken(userId: UUID, token: String, deviceInfo: String?) {
        repository.addOrUpdateToken(userId, token, deviceInfo)
    }

    suspend fun unregisterToken(token: String): Boolean {
        return repository.deactivateToken(token)
    }
}
