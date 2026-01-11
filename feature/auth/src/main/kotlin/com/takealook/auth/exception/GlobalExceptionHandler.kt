package com.takealook.auth.exception

import com.takealook.domain.exceptions.InvalidCredentialsException
import com.takealook.domain.exceptions.ProfileNotFoundException
import com.takealook.domain.exceptions.UserAlreadyExistsException
import com.takealook.domain.exceptions.UserNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentialsException(ex: InvalidCredentialsException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.UNAUTHORIZED
        log.warn("InvalidCredentialsException", ex)
        val errorResponse = ErrorResponse(
            status = status.value(),
            reason = "INVALID_CREDENTIALS",
            message = "Invalid username or password"
        )
        return ResponseEntity.status(status).body(errorResponse)
    }

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleUserAlreadyExistsException(ex: UserAlreadyExistsException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.CONFLICT
        log.warn("UserAlreadyExistsException", ex)
        val errorResponse = ErrorResponse(
            status = status.value(),
            reason = "USER_ALREADY_EXISTS",
            message = "User already exists"
        )
        return ResponseEntity.status(status).body(errorResponse)
    }

    @ExceptionHandler(ProfileNotFoundException::class)
    fun handleProfileNotFoundException(ex: ProfileNotFoundException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.NOT_FOUND
        log.warn("ProfileNotFoundException", ex)
        val errorResponse = ErrorResponse(
            status = status.value(),
            reason = "PROFILE_NOT_FOUND",
            message = "Profile not found"
        )
        return ResponseEntity.status(status).body(errorResponse)
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(ex: UserNotFoundException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.NOT_FOUND
        log.warn("UserNotFoundException", ex)
        val errorResponse = ErrorResponse(
            status = status.value(),
            reason = "USER_NOT_FOUND",
            message = "User not found"
        )
        return ResponseEntity.status(status).body(errorResponse)
    }

    data class ErrorResponse(
        val status: Int,
        val reason: String,
        val message: String
    )
}
