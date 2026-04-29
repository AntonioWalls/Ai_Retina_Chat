package com.antoniowalls.airetinachat.di

import com.antoniowalls.airetinachat.data.network.RetrofitClient
import com.antoniowalls.airetinachat.data.repository.AuthRepository
import com.antoniowalls.airetinachat.data.repository.ChatRepository
import com.antoniowalls.airetinachat.data.repository.HistoryRepositoryImpl
import com.antoniowalls.airetinachat.domain.repository.IHistoryRepository
import com.antoniowalls.airetinachat.domain.usecase.GetChatHistoryUseCase
import com.antoniowalls.airetinachat.viewmodel.AuthViewModel
import com.antoniowalls.airetinachat.viewmodel.ChatViewModel
import com.antoniowalls.airetinachat.viewmodel.HistoryViewModel
import com.antoniowalls.airetinachat.viewmodel.ProfileViewModel
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
    single { AuthRepository(get(), get(), get()) }
    single<IHistoryRepository> { HistoryRepositoryImpl(get(), get()) }

    //ChatRepository
    single { ChatRepository(get(), get(), get(), get()) }

    // UseCases
    factory { GetChatHistoryUseCase(get()) }

    // ViewModels
    viewModel { AuthViewModel(get()) }
    viewModel { HistoryViewModel(get()) }
    viewModel { ChatViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
}