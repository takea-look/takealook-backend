package com.takealook.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "계정 정보")
data class User(
    @Schema(description = "사용자 ID", example = "1")
    val id: Long? = null,
    @Schema(description = "사용자 이름(ID)", example = "user1")
    val username: String,
    @Schema(description = "비밀번호 (해시됨)", accessMode = Schema.AccessMode.WRITE_ONLY)
    val password: String,
    @Schema(description = "토스 사용자 고유 ID")
    val tossUserKey: Long? = null,
    @Schema(description = "실명 (복호화됨)", accessMode = Schema.AccessMode.WRITE_ONLY)
    val tossName: String? = null,
    @Schema(description = "전화번호 (복호화됨)", accessMode = Schema.AccessMode.WRITE_ONLY)
    val tossPhone: String? = null,
    @Schema(description = "이메일 (복호화됨)", accessMode = Schema.AccessMode.WRITE_ONLY)
    val tossEmail: String? = null
)

@Schema(description = "사용자 프로필 정보")
data class UserProfile(
    @Schema(description = "사용자 ID", example = "1")
    val id: Long? = null,
    @Schema(description = "사용자 이름(ID)", example = "user1")
    val username: String,
    @Schema(description = "닉네임", example = "길동이")
    val nickname: String? = null,
    @Schema(description = "프로필 이미지 URL", example = "http://example.com/profile.png")
    val image: String? = null,
    @Schema(description = "수정일시")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
