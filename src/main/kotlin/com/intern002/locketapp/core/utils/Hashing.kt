package com.intern002.locketapp.core.utils

import at.favre.lib.crypto.bcrypt.BCrypt

interface Hashing {
    fun hash(password: String): String
    fun verify(password: String, hash: String): Boolean
}

class BcryptHashing : Hashing {
    override fun hash(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    override fun verify(password: String, hash: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hash).verified
    }
}
