package com.takealook.data.user.profile

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserProfileR2dbcRepository : CoroutineCrudRepository<UserProfileEntity, Long> {

    @Query("""
        INSERT INTO user_profiles (user_id, username)
        VALUES (:id, :username)
    """)
    suspend fun insert(id: Long, username: String)

    @Query("""
        SELECT *
        FROM user_profiles
        WHERE nickname = :nickname
        LIMIT 1
    """)
    suspend fun findByNickname(nickname: String): UserProfileEntity?
}
