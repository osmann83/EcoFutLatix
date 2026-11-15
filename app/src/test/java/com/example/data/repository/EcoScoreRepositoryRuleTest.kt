package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias de repositorio y reglas de seguridad para EcoScoreRepository
 * ejecutadas localmente contra el emulador de Firestore.
 */
class EcoScoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun recordScore_authenticatedStudent_successfullyStoresScore() = runBlocking {
        signInTestUser(STUDENT_ALICE)
        val repository = EcoScoreRepository(firestore)

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.recordScore(
                nickname = "EcoHeroe12",
                school = "Liceo Ejército Pichincha",
                gameId = "basuraleza",
                score = 350L,
                itemsRecycled = 12L,
                accuracy = 92L
            )
        }

        assertTrue(result.isSuccess)
        val scoreId = result.getOrThrow()
        assertNotNull(scoreId)
    }

    @Test
    fun observeScoresByGame_authenticatedStudent_emitsMatchingGameScores() = runBlocking {
        signInTestUser(STUDENT_ALICE)
        val repository = EcoScoreRepository(firestore)

        val scoreResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.recordScore(
                nickname = "EcoHeroe12",
                school = "Liceo Ejército Pichincha",
                gameId = "basuraleza",
                score = 500L,
                itemsRecycled = 18L,
                accuracy = 95L
            )
        }
        assertTrue(scoreResult.isSuccess)

        val scores = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeScoresByGame("basuraleza").first { list ->
                list.any { it.nickname == "EcoHeroe12" }
            }
        }
        assertTrue(scores.isNotEmpty())
    }

    @Test
    fun recordScore_unauthenticatedUser_failsSecurityAssertion() = runBlocking {
        auth.signOut()
        val repository = EcoScoreRepository(firestore)

        val exception = assertThrows(IllegalStateException::class.java) {
            runBlocking {
                repository.recordScore(
                    nickname = "HackerAnon",
                    school = "Liceo Ejército Pichincha",
                    gameId = "basuraleza",
                    score = 9999L,
                    itemsRecycled = 50L,
                    accuracy = 100L
                )
            }
        }
        assertTrue(exception.message?.contains("iniciar sesión") == true)
    }

    private companion object {
        const val STUDENT_ALICE = "estudiante_alice@colegio.edu.co"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
