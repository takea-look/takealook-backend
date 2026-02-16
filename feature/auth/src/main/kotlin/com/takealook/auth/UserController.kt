package com.takealook.auth

import com.takealook.domain.exceptions.InvalidCredentialsException
import com.takealook.domain.exceptions.ProfileNotFoundException
import com.takealook.domain.user.profile.GetMyProfileUseCase
import com.takealook.domain.user.profile.GetUserProfileByIdUseCase
import com.takealook.domain.user.profile.UpdateMyProfileUseCase
import com.takealook.model.UserProfile
import io.jsonwebtoken.Claims
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "User", description = "사용자 관리 API")
@RestController
@RequestMapping("/user")
class UserController(
    private val getUserProfileByIdUseCase: GetUserProfileByIdUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val updateMyProfileUseCase: UpdateMyProfileUseCase,
) {

    data class UpdateMyProfileRequest(
        @field:Schema(description = "설정할 닉네임", example = "takea_look")
        val nickname: String? = null,
        @field:Schema(description = "설정할 프로필 이미지 URL", example = "https://cdn.example.com/avatar.png")
        val imageUrl: String? = null,
    )

    @Operation(
        summary = "내 프로필 수정",
        description = "닉네임은 최초 1회만 설정 가능하며(이미 값이 있으면 변경 불가). 닉네임은 금칙어/중복/길이(2~16자) 검증을 거칩니다."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
        ApiResponse(responseCode = "400", description = "요청값이 유효하지 않음 또는 닉네임 수정 불가"),
        ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 토큰 오류)"),
        ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
        ApiResponse(responseCode = "409", description = "닉네임이 이미 사용 중")
    ])
    @PatchMapping("/profile/me")
    suspend fun updateMyProfile(
        @AuthenticationPrincipal principal: Claims?,
        @RequestBody body: UpdateMyProfileRequest,
    ): ResponseEntity<UserProfile> {
        val username = principal?.subject?.takeIf { it.isNotBlank() }
            ?: throw InvalidCredentialsException("Invalid token")

        val profile = updateMyProfileUseCase(username, body.nickname, body.imageUrl)
        return ResponseEntity.ok(profile)
    }

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
