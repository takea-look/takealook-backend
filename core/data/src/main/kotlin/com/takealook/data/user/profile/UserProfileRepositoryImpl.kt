package com.takealook.data.user.profile

import com.takealook.domain.user.profile.UserProfileRepository
import com.takealook.model.UserProfile
import org.springframework.stereotype.Repository

@Repository
class UserProfileRepositoryImpl(
    private val repository: UserProfileR2dbcRepository
) : UserProfileRepository {
    override suspend fun insert(userProfile: UserProfile) {
        return repository.insert(
            id = userProfile.id ?: 0,
            username = userProfile.username
        )
    }

    override suspend fun save(user: UserProfile): UserProfile {
        return repository.save(user.toProfileEntity()).toProfile()
    }

    override suspend fun findByUserId(userId: Long): UserProfile? {
        return repository.findById(userId)?.toProfile()
    }
}
