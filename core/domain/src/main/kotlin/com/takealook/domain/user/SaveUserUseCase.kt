package com.takealook.domain.user

import com.takealook.data.user.UserRepository
import org.springframework.stereotype.Component

@Component
class SaveUserUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(user: User): User = repository
        .save(user.toUserEntity())
        .toUser()
}
