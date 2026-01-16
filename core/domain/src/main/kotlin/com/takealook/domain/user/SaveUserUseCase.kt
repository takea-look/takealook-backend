package com.takealook.domain.user

import com.takealook.domain.user.profile.UserProfileRepository
import com.takealook.model.User
import com.takealook.model.UserProfile

class SaveUserUseCase(
    private val repository: UserRepository,
    private val profileRepository: UserProfileRepository
) {
    suspend operator fun invoke(user: User) {
        val saved = repository.save(user)
        val userId = saved.id ?: 0

        val existingProfile = profileRepository.findByUserId(userId)

        if (existingProfile == null) {
            val newProfile = UserProfile(
                id = userId,
                username = saved.username,
            )
            profileRepository.insert(newProfile)
        } else {
            val updatedProfile = existingProfile.copy(
                username = saved.username
            )
            profileRepository.save(updatedProfile)
        }
    }
}
