package com.takealook.domain

import com.takealook.data.UserEntity

data class User(
    val id: Long? = null,
    val username: String,
    val password: String,
)

fun UserEntity.toUser() = User(
    id = id,
    username = username,
    password = password
)

fun User.toUserEntity() = UserEntity(
    id = this.id,
    username = username,
    password = this.password
)

