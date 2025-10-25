package com.takealook.auth

import com.takealook.domain.exceptions.ProfileNotFoundException
import com.takealook.domain.user.profile.GetUserProfileByIdUseCase
import com.takealook.model.UserProfile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserController(
    private val getUserProfileByIdUseCase: GetUserProfileByIdUseCase,
) {

    @GetMapping("/profile")
    suspend fun getUserById(
        @RequestParam(required = true) userId: Long
    ): ResponseEntity<UserProfile> {
        val profile = getUserProfileByIdUseCase(userId)
        if (profile == null) {
            throw ProfileNotFoundException("profile을 찾을 수 없습니다. 올바른 user Id를 입력해주세요.")
        }
        return ResponseEntity.ok(profile)
    }
}
