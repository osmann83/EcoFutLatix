package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.GameScore
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Repositorio para guardar puntajes de minijuegos y observar la tabla de posiciones global
 * (Leaderboard) en tiempo real desde Firestore.
 */
class EcoScoreRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("El usuario debe iniciar sesión con Google antes de guardar un puntaje.")
    }

    /**
     * Guarda el puntaje obtenido por el estudiante tras completar una partida en cualquiera de los 3 minijuegos.
     */
    suspend fun recordScore(
        nickname: String,
        school: String,
        gameId: String,
        score: Long,
        itemsRecycled: Long,
        accuracy: Long
    ): Result<String> {
        val uid = requireUserId()
        val scoreId = "score_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val docRef = db.collection("scores").document(scoreId)

        val scoreData = mapOf(
            "userId" to uid,
            "nickname" to nickname,
            "school" to school,
            "gameId" to gameId,
            "score" to score,
            "itemsRecycled" to itemsRecycled,
            "accuracy" to accuracy,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            docRef.set(scoreData).await()
            Result.success(scoreId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Observa en tiempo real los puntajes de un minijuego específico para la tabla de clasificación.
     */
    fun observeScoresByGame(gameId: String): Flow<List<GameScore>> {
        val query = db.collection("scores")
            .whereEqualTo("gameId", gameId)

        return query.snapshots().map { snapshot ->
            snapshot.toObjects(GameScore::class.java)
                .sortedByDescending { it.score }
        }
    }

    /**
     * Obtiene los puntajes históricos del usuario actual.
     */
    fun observeUserScores(userId: String = requireUserId()): Flow<List<GameScore>> {
        val query = db.collection("scores")
            .whereEqualTo("gameId", "basuraleza")

        return query.snapshots().map { snapshot ->
            snapshot.toObjects(GameScore::class.java)
                .filter { it.userId == userId }
                .sortedByDescending { it.createdAt }
        }
    }
}
