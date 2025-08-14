package com.takealook.domain.user

import com.takealook.model.User

interface UserRepository {
    suspend fun save(user: User): User
    suspend fun findByUserName(username: String): User?
    suspend fun findByUserId(userId: Long): User?
}
