package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BinCategory
import com.example.data.model.UserProfile
import com.example.data.model.WasteCatalogue
import com.example.data.model.WasteItem
import com.example.data.repository.EcoScoreRepository
import com.example.data.repository.StudentProfileRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Minijuego 1: Basuraleza.
 * Juego dinámico de separación de residuos en los 3 contenedores según la norma colombiana.
 * Cuenta con temporizador, combos, retroalimentación educativa y registro en Firebase.
 */
@Composable
fun BasuralezaGameScreen(
    userProfile: UserProfile,
    scoreRepository: EcoScoreRepository,
    profileRepository: StudentProfileRepository,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackToMenu() }

    val scope = rememberCoroutineScope()
    var timeLeft by remember { mutableIntStateOf(45) }
    var isGameOver by remember { mutableStateOf(false) }
    var currentItemIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableLongStateOf(0L) }
    var combo by remember { mutableIntStateOf(1) }
    var correctCount by remember { mutableIntStateOf(0) }
    var totalAttempts by remember { mutableIntStateOf(0) }
    var isSavingScore by remember { mutableStateOf(false) }
    var scoreSavedNotice by remember { mutableStateOf<String?>(null) }

    // Feedback visual momentáneo
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var feedbackIsCorrect by remember { mutableStateOf(true) }

    val scaleAnim = remember { Animatable(1f) }

    // Lista barajada de residuos
    val gameItems = remember { WasteCatalogue.items.shuffled() }
    val currentItem: WasteItem = gameItems[currentItemIndex % gameItems.size]

    // Temporizador
    LaunchedEffect(isGameOver) {
        if (!isGameOver) {
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft--
            }
            isGameOver = true
        }
    }

    // Al finalizar el juego, guardar puntaje automáticamente en Firebase
    LaunchedEffect(isGameOver) {
        if (isGameOver && score > 0 && !isSavingScore) {
            isSavingScore = true
            val accuracy = if (totalAttempts > 0) (correctCount * 100L / totalAttempts) else 0L
            scope.launch {
                scoreRepository.recordScore(
                    nickname = userProfile.nickname,
                    school = userProfile.school,
                    gameId = "basuraleza",
                    score = score,
                    itemsRecycled = correctCount.toLong(),
                    accuracy = accuracy
                ).fold(
                    onSuccess = {
                        profileRepository.incrementTotalScore(score)
                        scoreSavedNotice = "¡Puntaje registrado en la tabla global de Firebase!"
                        isSavingScore = false
                    },
                    onFailure = { error ->
                        scoreSavedNotice = "Guardado localmente. (${error.message})"
                        isSavingScore = false
                    }
                )
            }
        }
    }

    fun handleBinSelection(selectedCategory: BinCategory) {
        if (isGameOver) return

        totalAttempts++
        if (currentItem.category == selectedCategory) {
            val gainedPoints = 50L * combo
            score += gainedPoints
            correctCount++
            combo = (combo + 1).coerceAtMost(5)
            feedbackIsCorrect = true
            feedbackMessage = "¡Correcto! +$gainedPoints pts (Combo x$combo)\n${currentItem.ecoTip}"
        } else {
            combo = 1
            score = (score - 10L).coerceAtLeast(0L)
            feedbackIsCorrect = false
            feedbackMessage = "¡Incorrecto! La caneca adecuada es ${currentItem.category.containerColorName} (${currentItem.category.title})."
        }

        scope.launch {
            scaleAnim.animateTo(1.15f, tween(100, easing = FastOutSlowInEasing))
            scaleAnim.animateTo(1f, tween(100))
        }

        currentItemIndex++
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Cabecera del juego
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackToMenu) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver al Menú"
                    )
                }

                Text(
                    text = "Basuraleza",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Indicador de combo
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (combo > 1) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "x$combo",
                        fontWeight = FontWeight.Bold,
                        color = if (combo > 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Marcador y Temporizador
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = if (timeLeft <= 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tiempo: ${timeLeft}s",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (timeLeft <= 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "$score pts",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { timeLeft / 45f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (timeLeft <= 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }

            if (!isGameOver) {
                // Tarjeta central con el residuo a clasificar
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(scaleAnim.value)
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentItem.iconEmoji,
                            fontSize = 68.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                        Text(
                            text = currentItem.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Degradación: ${currentItem.decompositionYears}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Mensaje de retroalimentación educativa
                AnimatedVisibility(
                    visible = feedbackMessage != null,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (feedbackIsCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (feedbackIsCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (feedbackIsCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = feedbackMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (feedbackIsCorrect) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                            )
                        }
                    }
                }

                // Los 3 Contenedores interactivos (Resolución 2184)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "¿A qué caneca corresponde?",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Caneca Blanca (Aprovechables)
                        CanecaButton(
                            title = "Blanco",
                            subtitle = "Aprovechables",
                            containerColor = Color(0xFFECEFF1),
                            textColor = Color(0xFF263238),
                            borderColor = Color(0xFFB0BEC5),
                            modifier = Modifier.weight(1f).testTag("bin_blanco"),
                            onClick = { handleBinSelection(BinCategory.APROVECHABLE) }
                        )

                        // 2. Caneca Verde (Orgánicos)
                        CanecaButton(
                            title = "Verde",
                            subtitle = "Orgánicos",
                            containerColor = Color(0xFF4CAF50),
                            textColor = Color.White,
                            borderColor = Color(0xFF2E7D32),
                            modifier = Modifier.weight(1f).testTag("bin_verde"),
                            onClick = { handleBinSelection(BinCategory.ORGANICO) }
                        )

                        // 3. Caneca Negra (No aprovechables)
                        CanecaButton(
                            title = "Negro",
                            subtitle = "No aprovechables",
                            containerColor = Color(0xFF212121),
                            textColor = Color.White,
                            borderColor = Color.Black,
                            modifier = Modifier.weight(1f).testTag("bin_negro"),
                            onClick = { handleBinSelection(BinCategory.NO_APROVECHABLE) }
                        )
                    }
                }
            } else {
                // Pantalla de Fin de Juego con estadísticas
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "¡Partida Completada!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Puntaje Obtenido: $score pts",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Residuos clasificados: $correctCount de $totalAttempts",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        val accuracyPercent = if (totalAttempts > 0) (correctCount * 100 / totalAttempts) else 0
                        Text(
                            text = "Precisión ambiental: $accuracyPercent%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        if (scoreSavedNotice != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = scoreSavedNotice ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    timeLeft = 45
                                    score = 0L
                                    combo = 1
                                    correctCount = 0
                                    totalAttempts = 0
                                    feedbackMessage = null
                                    scoreSavedNotice = null
                                    isGameOver = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reintentar")
                            }

                            Button(
                                onClick = onBackToMenu,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors()
                            ) {
                                Text("Menú")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CanecaButton(
    title: String,
    subtitle: String,
    containerColor: Color,
    textColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(2.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = title,
                tint = textColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                fontSize = 10.sp
            )
        }
    }
}
