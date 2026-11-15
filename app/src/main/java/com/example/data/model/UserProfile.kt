package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Modelo de datos del perfil de estudiante para EcoFutLatix.
 * Todos los campos disponen de valores por defecto requeridos por Firestore.
 */
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val nickname: String = "",
    val school: String = "",
    val birthDate: String = "",
    val totalScore: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
