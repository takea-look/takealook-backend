package com.takealook.domain.user

import com.takealook.domain.user.profile.UserProfileRepository
import com.takealook.model.User
import com.takealook.model.UserProfile

class SaveUserUseCase(
    private val repository: UserRepository,
    private val profileRepository: UserProfileRepository
) {
    suspend operator fun invoke(user: User) {
        repository.save(user)
        val saved = repository.findByUserName(user.username)

        if (saved == null) return

        val newProfile = UserProfile(
            id = saved.id ?: 0,
            username = saved.username,
        )
        profileRepository.insert(newProfile)
    }
}
