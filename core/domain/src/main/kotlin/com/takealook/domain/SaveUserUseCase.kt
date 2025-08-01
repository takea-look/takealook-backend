package com.takealook.domain

import com.takealook.data.UserRepository
import org.springframework.stereotype.Component

@Component
class SaveUserUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(user: User): User = repository
        .save(user.toUserEntity())
        .toUser()
}
