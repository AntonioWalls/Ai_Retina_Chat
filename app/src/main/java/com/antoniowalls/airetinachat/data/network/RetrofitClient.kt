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
    //Pega aquí la URL que te acaba de dar Colab/Ngrok.
    private const val BASE_URL = "https://TU_URL_AQUI.ngrok-free.app/"

    // Configuramos un cliente OkHttp ultra-paciente para la IA
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                // Evita la pantalla de advertencia de Ngrok que bloquea los JSON
                .addHeader("ngrok-skip-browser-warning", "true")
                .build()
            chain.proceed(request)
        }
        // Le damos hasta 2 MINUTOS a la IA para pensar (ideal para análisis de imágenes pesadas)
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Le pasamos nuestro cliente paciente
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}