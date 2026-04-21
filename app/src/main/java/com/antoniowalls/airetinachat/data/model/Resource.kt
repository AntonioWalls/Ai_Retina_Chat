package com.antoniowalls.airetinachat.data.model

sealed class Resource<out T>{

    //Contendrá los datos si todo sale bien
    data class Success<out T>(val data: T) : Resource<T>()

    //Excepción si algo falla
    data class Error(val exception: Exception) : Resource<Nothing>()

    //muestra un mensaje de cargando
    object Loading : Resource<Nothing>()

}