package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.UserProfile
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

/**
 * Repositorio para la gestión de perfiles de estudiantes en Firestore.
 * Sigue la arquitectura limpia y maneja la persistencia con aislamiento por usuario.
 */
class StudentProfileRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("El usuario debe iniciar sesión con Google antes de acceder a su perfil.")
    }

    /**
     * Obtiene el perfil actual del estudiante autenticado.
     */
    suspend fun getProfile(userId: String = requireUserId()): Result<UserProfile?> {
        val docRef = db.collection("users").document(userId)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                Result.success(snapshot.toObject(UserProfile::class.java))
            } else {
                Result.success(null)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Observa en tiempo real los cambios en el perfil del usuario.
     */
    fun observeProfile(userId: String = requireUserId()): Flow<UserProfile?> {
        val docRef = db.collection("users").document(userId)
        return docRef.snapshots().map { snapshot ->
            if (snapshot.exists()) {
                snapshot.toObject(UserProfile::class.java)
            } else {
                null
            }
        }
    }

    /**
     * Registra o actualiza el perfil del estudiante en la base de datos de Firebase.
     */
    suspend fun saveProfile(
        nickname: String,
        school: String,
        birthDate: String
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)

        return try {
            val existing = docRef.get().await()
            if (existing.exists()) {
                // Actualización preservando createdAt
                val updateData = mapOf(
                    "nickname" to nickname,
                    "school" to school,
                    "birthDate" to birthDate,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updateData).await()
            } else {
                // Creación de nuevo perfil
                val createData = mapOf(
                    "userId" to uid,
                    "nickname" to nickname,
                    "school" to school,
                    "birthDate" to birthDate,
                    "totalScore" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createData).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Incrementa el puntaje acumulado total del estudiante.
     */
    suspend fun incrementTotalScore(points: Long): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        return try {
            docRef.update(
                mapOf(
                    "totalScore" to FieldValue.increment(points),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }
}
