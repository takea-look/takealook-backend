package com.takealook

import com.takealook.data.chat.message.ChatMessagesRepositoryImpl
import com.takealook.data.chat.room.ChatRoomsRepositoryImpl
import com.takealook.data.chat.users.ChatRoomUsersRepositoryImpl
import com.takealook.data.sticker.StickerCategoryRepositoryImpl
import com.takealook.data.sticker.StickerRepositoryImpl
import com.takealook.data.user.UserRepositoryImpl
import com.takealook.data.user.profile.UserProfileRepositoryImpl
import com.takealook.domain.chat.message.GetMessagesUseCase
import com.takealook.domain.chat.message.SaveMessageUseCase
import com.takealook.domain.chat.room.GetChatRoomsUseCase
import com.takealook.domain.chat.users.GetChatUsersByRoomIdUseCase
import com.takealook.domain.sticker.GetStickerCategoriesUseCase
import com.takealook.domain.sticker.GetStickersUseCase
import com.takealook.domain.sticker.SaveStickerCategoryUseCase
import com.takealook.domain.sticker.SaveStickerUseCase
import com.takealook.domain.user.GetUserByNameUseCase
import com.takealook.domain.user.SaveUserUseCase
import com.takealook.domain.user.profile.GetMyProfileUseCase
import com.takealook.domain.user.profile.GetUserProfileByIdUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TklBeanConfiguration(
    private val userRepository: UserRepositoryImpl,
    private val userProfileRepository: UserProfileRepositoryImpl,
    private val stickerRepository: StickerRepositoryImpl,
    private val stickerCategoryRepository: StickerCategoryRepositoryImpl,
    private val chatRoomUsersRepository: ChatRoomUsersRepositoryImpl,
    private val chatMessagesRepository: ChatMessagesRepositoryImpl,
    private val chatRoomsRepository: ChatRoomsRepositoryImpl,
) {
    @Bean
    fun provideSaveUserUseCase() = SaveUserUseCase(userRepository, userProfileRepository)

    @Bean
    fun provideGetUserByIdUseCase() = GetUserProfileByIdUseCase(userProfileRepository)

    @Bean
    fun provideGetMyProfileUseCase() = GetMyProfileUseCase(userRepository, userProfileRepository)

    @Bean
    fun provideGetUserByNameUseCase() = GetUserByNameUseCase(userRepository)

    @Bean
    fun provideStickerRepository() = SaveStickerUseCase(stickerRepository)

    @Bean
    fun provideGetStickersUseCase() = GetStickersUseCase(stickerRepository)

    @Bean
    fun provideStickerCategoryRepository() = GetStickerCategoriesUseCase(stickerCategoryRepository)

    @Bean
    fun provideSaveStickerCategoryUseCase() = SaveStickerCategoryUseCase(stickerCategoryRepository)

    @Bean
    fun provideGetChatUsersByRoomIdUseCase() = GetChatUsersByRoomIdUseCase(chatRoomUsersRepository)

    @Bean
    fun provideSaveChatMessageUseCase() = SaveMessageUseCase(chatMessagesRepository)

    @Bean
    fun provideGetChatMessagesUseCase() = GetMessagesUseCase(
        chatMessagesRepository = chatMessagesRepository,
        userProfileRepository = userProfileRepository,
    )

    @Bean
    fun provideGetChatRoomsUseCase() = GetChatRoomsUseCase(chatRoomsRepository)
}