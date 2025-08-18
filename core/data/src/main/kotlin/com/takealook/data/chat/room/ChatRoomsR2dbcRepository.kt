package com.takealook.data.chat.room

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ChatRoomsR2dbcRepository : CoroutineCrudRepository<ChatRoomsEntity, Long>