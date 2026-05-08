package com.example.dsm104_test3.model

import com.google.gson.annotations.SerializedName

data class Recurso(
    val id: String = "",
    @SerializedName("titulo") val titulo: String = "",
    @SerializedName("descripcion") val descripcion: String = "",
    @SerializedName("tipo") val tipo: String = "",
    @SerializedName("enlace") val enlace: String = "",
    @SerializedName("imagen") val imagen: String = "",
    @SerializedName("rating") val rating: Float = 0f,
    @SerializedName("totalRatings") val totalRatings: Int = 0
)
