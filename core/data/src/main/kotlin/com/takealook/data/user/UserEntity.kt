package com.takealook.data.user

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class UserEntity(
    @Id
    val id: Long? = null,
    val username: String,
    val password: String,
)