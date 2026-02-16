package com.takealook.auth

import com.takealook.domain.exceptions.ProfileNicknameConflictException
import com.takealook.domain.exceptions.ProfileNotFoundException
import com.takealook.domain.exceptions.ProfileUpdateNotAllowedException
import com.takealook.domain.user.UserRepository
import com.takealook.domain.user.profile.UpdateMyProfileUseCase
import com.takealook.domain.user.profile.UserProfileRepository
import com.takealook.model.User
import com.takealook.model.UserProfile
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class UpdateMyProfileUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val userProfileRepository = mockk<UserProfileRepository>(relaxed = true)
    private val useCase = UpdateMyProfileUseCase(userRepository, userProfileRepository)

    @Test
    fun `nickname can be set first time and persists`() = runBlocking {
        val user = User(id = 10L, username = "u", password = "pw")
        val existingProfile = UserProfile(id = 10L, username = "u", nickname = null, image = null)
        val savedProfile = existingProfile.copy(nickname = "newNick")

        coEvery { userRepository.findByUserName("u") } returns user
        coEvery { userProfileRepository.findByUserId(10L) } returns existingProfile
        coEvery { userProfileRepository.findByNickname("newNick") } returns null
        coEvery { userProfileRepository.save(any()) } returns savedProfile

        val result = useCase("u", " newNick ", null)

        assertEquals("newNick", result.nickname)
        coVerify(exactly = 1) { userProfileRepository.save(any()) }
    }

    @Test
    fun `nickname cannot be changed once set`() = runBlocking {
        val user = User(id = 10L, username = "u", password = "pw")
        val existingProfile = UserProfile(id = 10L, username = "u", nickname = "oldNick", image = null)

        coEvery { userRepository.findByUserName("u") } returns user
        coEvery { userProfileRepository.findByUserId(10L) } returns existingProfile

        assertThrows(ProfileUpdateNotAllowedException::class.java) {
            runBlocking {
                useCase("u", "newNick", null)
            }
        }
    }

    @Test
    fun `nickname conflict should throw conflict exception`() = runBlocking {
        val user = User(id = 10L, username = "u", password = "pw")
        val existingProfile = UserProfile(id = 10L, username = "u", nickname = null, image = null)
        val otherProfile = UserProfile(id = 11L, username = "other", nickname = "dupnick", image = null)

        coEvery { userRepository.findByUserName("u") } returns user
        coEvery { userProfileRepository.findByUserId(10L) } returns existingProfile
        coEvery { userProfileRepository.findByNickname("dupnick") } returns otherProfile

        assertThrows(ProfileNicknameConflictException::class.java) {
            runBlocking {
                useCase("u", "dupnick", null)
            }
        }
    }

    @Test
    fun `missing user should throw profile not found`() = runBlocking {
        coEvery { userRepository.findByUserName("u") } returns null

        assertThrows(ProfileNotFoundException::class.java) {
            runBlocking {
                useCase("u", "nick", null)
            }
        }
    }
}
