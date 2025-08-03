package com.takealook.domain.user

import com.takealook.data.user.UserEntity

data class User(
    val id: Long? = null,
    val username: String,
    val password: String,
)

fun UserEntity.toUser() = User(
    username = username,
    password = password
)

fun User.toUserEntity() = UserEntity(
    id = this.id,
    username = username,
    password = this.password
)

