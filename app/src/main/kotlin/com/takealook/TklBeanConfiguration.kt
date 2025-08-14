package com.takealook

import com.takealook.data.sticker.StickerCategoryRepositoryImpl
import com.takealook.data.sticker.StickerRepositoryImpl
import com.takealook.data.user.UserRepositoryImpl
import com.takealook.domain.sticker.GetStickerCategoriesUseCase
import com.takealook.domain.sticker.GetStickersUseCase
import com.takealook.domain.sticker.SaveStickerCategoryUseCase
import com.takealook.domain.sticker.SaveStickerUseCase
import com.takealook.domain.user.GetUserByIdUseCase
import com.takealook.domain.user.GetUserByNameUseCase
import com.takealook.domain.user.SaveUserUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TklBeanConfiguration(
    private val userRepository: UserRepositoryImpl,
    private val stickerRepository: StickerRepositoryImpl,
    private val stickerCategoryRepository: StickerCategoryRepositoryImpl
) {
    @Bean
    fun provideSaveUserUseCase() = SaveUserUseCase(userRepository)

    @Bean
    fun provideGetUserByIdUseCase() = GetUserByIdUseCase(userRepository)

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
}