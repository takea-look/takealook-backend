package com.takealook.model

import java.time.LocalDateTime

data class User(
    val id: Long? = null,
    val username: String,
    val password: String,
)

data class UserProfile(
    val id: Long? = null,
    val username: String,
    val nickname: String? = null,
    val image: String? = null,
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
