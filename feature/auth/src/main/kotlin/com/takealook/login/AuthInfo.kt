package com.takealook.login

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class AuthInfo(
    @Id
    val id: Long? = null,
    val username: String,
    val password: String,
)