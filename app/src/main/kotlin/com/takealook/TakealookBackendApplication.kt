package com.takealook

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TakealookBackendApplication

fun main(args: Array<String>) {
    runApplication<TakealookBackendApplication>(*args)
}
