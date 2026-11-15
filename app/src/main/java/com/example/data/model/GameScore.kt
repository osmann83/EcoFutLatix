package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Modelo de datos para registrar los puntajes y estadísticas de cada partida
 * en los minijuegos de reciclaje (Basuraleza, Búsqueda del Tesoro, Encestar).
 */
@IgnoreExtraProperties
data class GameScore(
    @DocumentId val id: String = "",
    val userId: String = "",
    val nickname: String = "",
    val school: String = "",
    val gameId: String = "", // "basuraleza", "busqueda_tesoro", "encestar", "general"
    val score: Long = 0L,
    val itemsRecycled: Long = 0L,
    val accuracy: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
