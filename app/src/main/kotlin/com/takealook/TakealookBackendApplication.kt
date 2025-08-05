package com.takealook

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(
    exclude = [
        SecurityAutoConfiguration::class,
        SecurityFilterAutoConfiguration::class
    ]
)
class TakealookBackendApplication

fun main(args: Array<String>) {
    runApplication<TakealookBackendApplication>(*args)
}
