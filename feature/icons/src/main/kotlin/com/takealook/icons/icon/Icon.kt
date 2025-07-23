package com.takealook.icons.icon

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("icons")
data class Icon(
    @Id val id: Int? = null,
    val name: String,
    val iconUrl: String,
    val thumbnailUrl: String,
    val categoryId: Int
)
