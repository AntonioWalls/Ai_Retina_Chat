package com.antoniowalls.airetinachat.di

import com.antoniowalls.airetinachat.data.network.RetrofitClient
import com.antoniowalls.airetinachat.data.repository.AuthRepositoryImpl
import com.antoniowalls.airetinachat.data.repository.ChatRepositoryImpl
import com.antoniowalls.airetinachat.data.repository.HistoryRepositoryImpl
import com.antoniowalls.airetinachat.domain.repository.IHistoryRepository
import com.antoniowalls.airetinachat.domain.usecase.history.GetChatHistoryUseCase
import com.antoniowalls.airetinachat.domain.usecase.auth.GetCurrentUserUseCase
import com.antoniowalls.airetinachat.domain.usecase.auth.LoginWithEmailUseCase
import com.antoniowalls.airetinachat.domain.usecase.auth.LoginWithGoogleUseCase
import com.antoniowalls.airetinachat.domain.usecase.auth.LogoutUseCase
import com.antoniowalls.airetinachat.domain.usecase.auth.RegisterWithEmailUseCase
import com.antoniowalls.airetinachat.domain.usecase.auth.ResetPasswordUseCase
import com.antoniowalls.airetinachat.domain.usecase.chat.GetChatSessionUseCase
import com.antoniowalls.airetinachat.domain.usecase.chat.GetCurrentUserIdUseCase
import com.antoniowalls.airetinachat.domain.usecase.chat.SaveChatSessionUseCase
import com.antoniowalls.airetinachat.domain.usecase.chat.SendMessageToAIUseCase
import com.antoniowalls.airetinachat.domain.usecase.chat.UploadImageToCloudUseCase
import com.antoniowalls.airetinachat.domain.usecase.history.FilterAndGroupHistoryUseCase
import com.antoniowalls.airetinachat.domain.usecase.profile.ChangePasswordUseCase
import com.antoniowalls.airetinachat.domain.usecase.profile.GetExtraProfileDataUseCase
import com.antoniowalls.airetinachat.domain.usecase.profile.GetLastSignInTimestampUseCase
import com.antoniowalls.airetinachat.domain.usecase.profile.ReAuthenticateAndChangePasswordUseCase
import com.antoniowalls.airetinachat.domain.usecase.profile.Update2FAStateUseCase
import com.antoniowalls.airetinachat.domain.usecase.profile.UpdateProfileUseCase
import com.antoniowalls.airetinachat.presentation.viewmodel.AuthViewModel
import com.antoniowalls.airetinachat.presentation.viewmodel.ChatViewModel
import com.antoniowalls.airetinachat.presentation.viewmodel.HistoryViewModel
import com.antoniowalls.airetinachat.presentation.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Firebase instances
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance() }

    //Retrofit API Service
    single { RetrofitClient.apiService }

    // Repositories
    single { AuthRepositoryImpl(get(), get(), get()) }
    single<IHistoryRepository> { HistoryRepositoryImpl(get(), get()) }

    //ChatRepository
    single { ChatRepositoryImpl(get(), get(), get(), get()) }

    // UseCases
    //UseCases de autenticación
    factory { LoginWithEmailUseCase(get()) }
    factory { RegisterWithEmailUseCase(get()) }
    factory { LoginWithGoogleUseCase(get()) }
    factory { ResetPasswordUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { GetCurrentUserUseCase(get()) }

    //UseCases de Chat
    factory { UploadImageToCloudUseCase(get()) }
    factory { SendMessageToAIUseCase(get()) }
    factory { SaveChatSessionUseCase(get()) }
    factory { GetChatSessionUseCase(get()) }
    factory { GetCurrentUserIdUseCase(get()) }

    //UseCases de Historial
    factory { GetChatHistoryUseCase(get()) }
    factory { FilterAndGroupHistoryUseCase() }

    //UseCases de perfil
    factory { GetExtraProfileDataUseCase(get()) }
    factory { UpdateProfileUseCase(get()) }
    factory { ChangePasswordUseCase(get()) }
    factory { Update2FAStateUseCase(get()) }
    factory { ReAuthenticateAndChangePasswordUseCase(get()) }
    factory { GetLastSignInTimestampUseCase(get()) }

    // ViewModels
    viewModel { AuthViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { HistoryViewModel(get(), get()) }
    viewModel { ChatViewModel(get(), get(), get(), get(), get()) }
    viewModel {
        ProfileViewModel(get(), get(), get(), get(), get(), get(), get(), get())
    }
}