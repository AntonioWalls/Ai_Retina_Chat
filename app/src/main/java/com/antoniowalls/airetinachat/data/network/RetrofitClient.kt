package com.antoniowalls.airetinachat.data.network

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.concurrent.TimeUnit

// 1. Lo que esperamos que nos responda tu backend en Python
data class ChatResponse(
    val success: Boolean,
    val response: String?,
    val error: String?
)

// 2. Cómo le enviamos los datos a Python (endpoint /chat)
interface ApiService {
    @Multipart
    @POST("chat")
    suspend fun sendMessage(
        @Part("prompt") prompt: RequestBody,
        @Part file: MultipartBody.Part? // Puede ser null si el usuario solo manda texto sin foto
    ): ChatResponse
}

// 3. El cliente Singleton
object RetrofitClient {
    // ⚠️ ¡CRÍTICO! Actualiza esta URL cada vez que inicies Ngrok en Colab
    // Recuerda que DEBE terminar con una barra diagonal "/"
    private const val BASE_URL = "https://a83e-35-198-255-29.ngrok-free.app/"

    // Configuramos un cliente OkHttp personalizado
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                // Evita la pantalla de advertencia de Ngrok
                .addHeader("ngrok-skip-browser-warning", "true")
                .build()
            chain.proceed(request)
        }
        // Aumentamos los tiempos de espera porque la IA puede tardar en responder
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Le pasamos nuestro cliente personalizado
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}