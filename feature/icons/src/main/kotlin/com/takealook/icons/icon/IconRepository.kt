package com.takealook.icons.icon

import com.takealook.icons.icon.Icon

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface IconRepository : CoroutineCrudRepository<Icon, Int>
