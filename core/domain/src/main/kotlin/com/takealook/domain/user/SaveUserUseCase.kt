package com.takealook.domain.user

import com.takealook.model.User

class SaveUserUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(user: User): User = repository.save(user)
}
