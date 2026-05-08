package com.example.dsm104_test3.network

import com.example.dsm104_test3.model.Recurso
import com.example.dsm104_test3.model.Usuario
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- Recursos ---
    @GET("resources")
    suspend fun getRecursos(): Response<List<Recurso>>

    @GET("resources/{id}")
    suspend fun getRecurso(@Path("id") id: String): Response<Recurso>

    @POST("resources")
    suspend fun createRecurso(@Body recurso: Recurso): Response<Recurso>

    @PUT("resources/{id}")
    suspend fun updateRecurso(@Path("id") id: String, @Body recurso: Recurso): Response<Recurso>

    @DELETE("resources/{id}")
    suspend fun deleteRecurso(@Path("id") id: String): Response<Recurso>

    // --- Usuarios ---
    @GET("users")
    suspend fun getUsers(): Response<List<Usuario>>

    @POST("users")
    suspend fun createUser(@Body usuario: Usuario): Response<Usuario>

    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body usuario: Usuario): Response<Usuario>
}
