package com.takealook.data.chat

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("chat_rooms")
data class ChatRoomUsersEntity(
    @Id val id: Long? = null,
    val userId: Long,
    val roomId: Long,
    val joinedAt: Long
)
