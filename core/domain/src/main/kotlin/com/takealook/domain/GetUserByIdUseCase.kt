package com.takealook.domain

import com.takealook.data.UserRepository
import org.springframework.stereotype.Component

@Component
class GetUserByIdUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(id: Long): User? = repository
        .findById(id)
        ?.toUser()
}
