package com.takealook.data.user

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserR2dbcRepository : CoroutineCrudRepository<UserEntity, Long> {
    suspend fun findByUsername(username: String): UserEntity?
}
