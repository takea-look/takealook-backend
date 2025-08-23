package com.takealook.model

data class User(
    val id: Long? = null,
    val username: String,
    val password: String,
)

data class UserProfile(
    val id: Long? = null,
    val username: String,
    val image: String? = null,
)
