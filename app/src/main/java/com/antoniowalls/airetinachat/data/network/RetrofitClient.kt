package com.antoniowalls.airetinachat.data.network

import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// 1. Lo que esperamos que nos responda tu backend en Python
data class ChatResponse(
    val success: Boolean,
    val response: String?,
    val error: String?
)

// 2. Cómo le enviamos los datos a Python (endpoint /chat)
interface ApiService {
    @POST("chat")
    suspend fun sendMessage(
        @Body body: RequestBody // <-- SOLUCIÓN DEFINITIVA: Enviamos el cuerpo construido manualmente
    ): ChatResponse
}

// 3. El cliente Singleton
object RetrofitClient {
    // ⚠️ ¡NO OLVIDES PEGAR AQUÍ TU URL DE NGROK ACTUAL! ⚠️
    private const val BASE_URL = "https://4477-34-143-162-214.ngrok-free.app/"

    // Configuramos un cliente OkHttp ultra-paciente para la IA
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                // Evita la pantalla de advertencia de Ngrok que bloquea los JSON
                .addHeader("ngrok-skip-browser-warning", "true")
                .build()
            chain.proceed(request)
        }
        // Le damos hasta 2 MINUTOS a la IA para pensar
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}