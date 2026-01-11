package com.takealook.domain.user.profile

import com.takealook.domain.exceptions.UserNotFoundException
import com.takealook.domain.user.UserRepository
import com.takealook.model.UserProfile

/**
 * JWT 토큰에서 추출한 username을 사용하여 본인 프로필 정보를 조회합니다.
 */
class GetMyProfileUseCase(
    private val userRepository: UserRepository,
    private val userProfileRepository: UserProfileRepository
) {
    suspend operator fun invoke(username: String): UserProfile {
        // 1. username으로 User 조회
        val user = userRepository.findByUserName(username)
            ?: throw UserNotFoundException("User not found")

        // User exists but has no ID - this indicates a data integrity issue
        val userId = user.id ?: throw UserNotFoundException("User data is invalid")

        // 2. user.id로 UserProfile 조회
        val profile = userProfileRepository.findByUserId(userId)
            ?: throw UserNotFoundException("Profile not found")

        return profile
    }
}
