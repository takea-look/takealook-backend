package com.takealook.domain.user

import com.takealook.data.user.UserRepository
import org.springframework.stereotype.Component

@Component
class GetUserByNameUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(username: String): User? = repository
        .findByUsername(username)
        ?.toUser()
}
