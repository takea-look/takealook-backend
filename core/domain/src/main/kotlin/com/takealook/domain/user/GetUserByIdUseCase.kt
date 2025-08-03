package com.takealook.domain.user

import com.takealook.data.user.UserRepository
import org.springframework.stereotype.Component

@Component
class GetUserByIdUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(id: Long): User? = repository
        .findById(id)
        ?.toUser()
}
