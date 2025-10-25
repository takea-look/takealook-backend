package com.takealook.auth.exception

import com.takealook.domain.exceptions.InvalidCredentialsException
import com.takealook.domain.exceptions.ProfileNotFoundException
import com.takealook.domain.exceptions.UserAlreadyExistsException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentialsException(ex: InvalidCredentialsException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.UNAUTHORIZED
        val errorResponse = ErrorResponse(
            status = status.value(),
            reason = "INVALID_CREDENTIALS",
            message = ex.message ?: "Invalid username or password"
        )
        return ResponseEntity.status(status).body(errorResponse)
    }

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleUserAlreadyExistsException(ex: UserAlreadyExistsException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.CONFLICT
        val errorResponse = ErrorResponse(
            status = status.value(),
            reason = "USER_ALREADY_EXISTS",
            message = ex.message ?: "User already exists"
        )
        return ResponseEntity.status(status).body(errorResponse)
    }

    @ExceptionHandler(ProfileNotFoundException::class)
    fun handleProfileNotFoundException(ex: ProfileNotFoundException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.NOT_FOUND
        val errorResponse = ErrorResponse(
            status = status.value(),
            reason = "Profile not found",
            message = ex.message ?: "Profile not found"
        )
        return ResponseEntity.status(status).body(errorResponse)
    }

    data class ErrorResponse(
        val status: Int,
        val reason: String,
        val message: String
    )
}
