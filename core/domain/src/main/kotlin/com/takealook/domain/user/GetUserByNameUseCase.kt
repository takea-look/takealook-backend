package com.takealook.domain.user

import com.takealook.model.User

class GetUserByNameUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(username: String): User? = repository.findByUserName(username)
}
