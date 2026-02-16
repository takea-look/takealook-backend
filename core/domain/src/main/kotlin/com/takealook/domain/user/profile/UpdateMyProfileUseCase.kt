package com.takealook.domain.user.profile

import com.takealook.domain.exceptions.ProfileNotFoundException
import com.takealook.domain.exceptions.ProfileNicknameConflictException
import com.takealook.domain.exceptions.ProfileUpdateNotAllowedException
import com.takealook.model.UserProfile

class UpdateMyProfileUseCase(
    private val userRepository: com.takealook.domain.user.UserRepository,
    private val userProfileRepository: UserProfileRepository,
) {
    private val prohibitedNicknames = setOf("admin", "administrator", "운영자", "관리자", "administer")

    suspend operator fun invoke(username: String, nickname: String?, imageUrl: String?): UserProfile {
        val user = userRepository.findByUserName(username)
            ?: throw ProfileNotFoundException("user not found")

        val existing = userProfileRepository.findByUserId(user.id!!)
            ?: throw ProfileNotFoundException("profile not found")

        val normalizedNickname = nickname?.trim()
        if (nickname != null && normalizedNickname?.isBlank() == true) {
            throw IllegalArgumentException("nickname must not be blank")
        }

        if (normalizedNickname != null) {
            if (normalizedNickname.length < 2 || normalizedNickname.length > 16) {
                throw IllegalArgumentException("nickname length must be between 2 and 16")
            }

            if (prohibitedNicknames.any { it.equals(normalizedNickname, ignoreCase = true) }) {
                throw IllegalArgumentException("nickname contains prohibited word")
            }

            if (existing.nickname != null && existing.nickname != normalizedNickname) {
                throw ProfileUpdateNotAllowedException("nickname can be set only once")
            }

            val duplicate = userProfileRepository.findByNickname(normalizedNickname)
            if (duplicate != null && duplicate.id != user.id) {
                throw ProfileNicknameConflictException("nickname already taken")
            }
        }

        val updated = existing.copy(
            nickname = existing.nickname ?: normalizedNickname,
            image = imageUrl ?: existing.image,
        )

        return userProfileRepository.save(updated)
    }
}
