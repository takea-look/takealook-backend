package com.takealook.domain.user

import com.takealook.model.User

class GetUserByIdUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(id: Long): User? = repository.findByUserId(id)
}
