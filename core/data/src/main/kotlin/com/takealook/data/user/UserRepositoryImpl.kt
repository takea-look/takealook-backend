package com.takealook.data.user

import com.takealook.domain.user.UserRepository
import com.takealook.model.User
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
    private val repository: UserR2dbcRepository
) : UserRepository {
    override suspend fun save(user: User): User {
        return repository.save(user.toUserEntity()).toUser()
    }

    override suspend fun findByUserName(username: String): User? {
        return repository.findByUsername(username)?.toUser()
    }

    override suspend fun findByUserId(userId: Long): User? {
        return repository.findById(userId)?.toUser()
    }
}
