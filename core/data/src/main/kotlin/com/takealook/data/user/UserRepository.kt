package com.takealook.data.user

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRepository : CoroutineCrudRepository<UserEntity, Long> {
    suspend fun findByUsername(username: String): UserEntity?
}
