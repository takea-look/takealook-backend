package com.takealook.domain.user

import com.takealook.model.UserProfile

class GetUserProfileByIdUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(id: Long): UserProfile? = repository
        .findByUserId(id)
        ?.let {
            UserProfile(
                id = it.id,
                username = it.username,
            )
        }
}
