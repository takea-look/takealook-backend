package com.takealook.icons.category

import kotlinx.coroutines.flow.toList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/icon-categories")
class IconCategoryController @Autowired constructor(
    private val iconCategoryRepository: IconCategoryRepository
) {

    @PostMapping
    suspend fun createCategory(@RequestBody request: IconCategoryCreation): ResponseEntity<IconCategory> {
        val category = IconCategory(name = request.name, thumbnailUrl = request.thumbnailUrl)
        val savedCategory = iconCategoryRepository.save(category)
        return ResponseEntity.ok(savedCategory)
    }

    @GetMapping
    suspend fun getAllCategories(): ResponseEntity<List<IconCategory>> {
        val categories = iconCategoryRepository.findAll()
        return ResponseEntity.ok(categories.toList())
    }
}
