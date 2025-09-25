package com.takealook.storage

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/storage")
class StorageController(
    private val service: StorageService
) {

    @GetMapping("/upload")
    fun getUploadUrl(@RequestParam key: String) =
        mapOf("url" to service.generateUploadUrl(key))
}