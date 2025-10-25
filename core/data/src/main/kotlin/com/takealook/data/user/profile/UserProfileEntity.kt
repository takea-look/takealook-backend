package com.takealook.data.user.profile

import com.takealook.model.UserProfile
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("user_profiles")
data class UserProfileEntity(

    /**
     * [com.takealook.data.user.UserEntity.id]
     */
    @Id val userId: Long,

    /**
     * [com.takealook.data.user.UserEntity.username]
     */
    val username: String,

    /**
     * 유저의 닉네임
     */
    val nickname: String? = null,

    /**
     * 유저의 프로필 이미지 url
     */
    val imageUrl: String? = null,

    /**
     * 프로필 업데이트 일자
     */
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

fun UserProfileEntity.toProfile(): UserProfile {
    return UserProfile(
        id = userId,
        username = username,
        nickname = nickname,
        image = imageUrl,
        updatedAt = updatedAt,
    )
}

fun UserProfile.toProfileEntity(): UserProfileEntity {
    return UserProfileEntity(
        userId = id ?: 0,
        username = username,
        nickname = nickname,
        imageUrl = image,
        updatedAt = updatedAt
    )
}