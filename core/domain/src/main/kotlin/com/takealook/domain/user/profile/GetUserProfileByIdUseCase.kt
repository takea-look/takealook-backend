package com.takealook.domain.user.profile

import com.takealook.model.UserProfile

class GetUserProfileByIdUseCase(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(id: Long): UserProfile? = repository
        .findByUserId(id)
}
