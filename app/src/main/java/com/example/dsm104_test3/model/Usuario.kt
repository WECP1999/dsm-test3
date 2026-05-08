package com.example.dsm104_test3.model

import com.google.gson.annotations.SerializedName

data class Usuario(
    val id: String = "",
    @SerializedName("username") val username: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("password") val password: String = "",
    @SerializedName("role") val role: String = "estudiante",
    @SerializedName("favoritos") val favoritos: List<String> = emptyList(),
    @SerializedName("ratings") val ratings: Map<String, Float> = emptyMap()
)
