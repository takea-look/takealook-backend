package com.takealook.domain.user.profile

import com.takealook.domain.exceptions.ProfileNotFoundException
import com.takealook.domain.exceptions.ProfileUpdateNotAllowedException
import com.takealook.model.UserProfile

class UpdateMyProfileUseCase(
    private val userRepository: com.takealook.domain.user.UserRepository,
    private val userProfileRepository: UserProfileRepository,
) {
    suspend operator fun invoke(username: String, nickname: String?, imageUrl: String?): UserProfile {
        val user = userRepository.findByUserName(username)
            ?: throw ProfileNotFoundException("user not found")

        val existing = userProfileRepository.findByUserId(user.id!!)
            ?: throw ProfileNotFoundException("profile not found")

        if (nickname != null) {
            if (existing.nickname != null && existing.nickname != nickname) {
                throw ProfileUpdateNotAllowedException("nickname can be set only once")
            }
        }

        val updated = existing.copy(
            nickname = existing.nickname ?: nickname,
            image = imageUrl ?: existing.image,
        )

        return userProfileRepository.save(updated)
    }
}
