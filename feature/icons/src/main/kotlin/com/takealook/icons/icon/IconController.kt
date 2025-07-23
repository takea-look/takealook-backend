package com.takealook.icons.icon

import kotlinx.coroutines.flow.toList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/icons")
class IconController @Autowired constructor(
    private val iconRepository: IconRepository
) {

    @PostMapping
    suspend fun createIcon(@RequestBody request: IconCreation): ResponseEntity<Icon> {
        val icon = Icon(name = request.name, iconUrl = request.iconUrl, thumbnailUrl = request.thumbnailUrl, categoryId = request.categoryId)
        val savedIcon = iconRepository.save(icon)
        return ResponseEntity.ok(savedIcon)
    }

    @GetMapping
    suspend fun getAllIcons(): ResponseEntity<List<Icon>> {
        val icons = iconRepository.findAll()
        return ResponseEntity.ok(icons.toList())
    }
}
