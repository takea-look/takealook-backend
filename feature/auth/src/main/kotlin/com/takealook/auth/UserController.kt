package com.takealook.auth

import com.takealook.domain.exceptions.InvalidCredentialsException
import com.takealook.domain.exceptions.ProfileNotFoundException
import com.takealook.domain.user.profile.GetMyProfileUseCase
import com.takealook.domain.user.profile.GetUserProfileByIdUseCase
import com.takealook.model.UserProfile
import io.jsonwebtoken.Claims
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "User", description = "사용자 관리 API")
@RestController
@RequestMapping("/user")
class UserController(
    private val getUserProfileByIdUseCase: GetUserProfileByIdUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
) {

    @Operation(
        summary = "내 프로필 조회",
        description = "JWT 토큰을 통해 현재 로그인한 사용자의 프로필 정보를 조회합니다."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
        ApiResponse(responseCode = "404", description = "사용자 또는 프로필을 찾을 수 없음")
    ])
    @GetMapping("/profile/me")
    suspend fun getMyProfile(
        @AuthenticationPrincipal principal: Claims?
    ): ResponseEntity<UserProfile> {
        val username = principal?.subject?.takeIf { it.isNotBlank() }
            ?: throw InvalidCredentialsException("Invalid token")
        val profile = getMyProfileUseCase(username)
        return ResponseEntity.ok(profile)
    }

    @Operation(summary = "사용자 프로필 조회", description = "사용자 ID로 프로필 정보를 조회합니다.")
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
