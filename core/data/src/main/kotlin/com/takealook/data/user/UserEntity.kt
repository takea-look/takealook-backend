package com.takealook.data.user

import com.takealook.model.User
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class UserEntity(
    @Id
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