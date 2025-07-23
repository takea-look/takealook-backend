package com.takealook.icons.category

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("icon_categories")
data class IconCategory(
    @Id val id: Int? = null,
    val name: String,
    val thumbnailUrl: String
)
