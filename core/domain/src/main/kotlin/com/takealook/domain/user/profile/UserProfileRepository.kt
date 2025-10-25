package com.takealook.domain.user.profile

import com.takealook.model.UserProfile

/**
 * 유저 프로필 정보 (닉네임, 프로필 이미지 등)를 저장합니다.
 */
interface UserProfileRepository {
    /**
     * 유저 프로필 저장
     */
    suspend fun save(user: UserProfile): UserProfile

    /**
     * 유저 프로필 초기 추기
     */
    suspend fun insert(userProfile: UserProfile)

    /**
     * 유저 프로필 정보 조회
     */
    suspend fun findByUserId(userId: Long): UserProfile?
}