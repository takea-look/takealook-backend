package com.takealook.login

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface AuthRepository : CoroutineCrudRepository<AuthInfo, Long> {
    suspend fun findByUsername(username: String): AuthInfo?
}