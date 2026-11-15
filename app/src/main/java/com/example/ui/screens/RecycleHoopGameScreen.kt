package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BinCategory
import com.example.data.model.UserProfile
import com.example.data.model.WasteCatalogue
import com.example.data.repository.EcoScoreRepository
import com.example.data.repository.StudentProfileRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Minijuego 3: Encestar Residuos.
 * Juego de puntería y física donde el estudiante calcula la fuerza y el ángulo
 * para encestar residuos en los contenedores correspondientes.
 */
@Composable
fun RecycleHoopGameScreen(
    userProfile: UserProfile,
    scoreRepository: EcoScoreRepository,
    profileRepository: StudentProfileRepository,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackToMenu() }

    val scope = rememberCoroutineScope()
    var shotsLeft by remember { mutableIntStateOf(10) }
    var score by remember { mutableLongStateOf(0L) }
    var successfulHoops by remember { mutableIntStateOf(0) }
    var currentItemIndex by remember { mutableIntStateOf(0) }
    var isShooting by remember { mutableStateOf(false) }
    var lastShotResult by remember { mutableStateOf<String?>(null) }
    var isGameOver by remember { mutableStateOf(false) }
    var isSavingScore by remember { mutableStateOf(false) }

    // Control de puntería interactivo
    var powerSlider by remember { mutableFloatStateOf(50f) }

    // Oscilador de puntería
    val aimMeter = remember { Animatable(0f) }
    LaunchedEffect(isShooting, isGameOver) {
        if (!isShooting && !isGameOver) {
            aimMeter.animateTo(
                targetValue = 100f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }

    val items = remember { WasteCatalogue.items.shuffled() }
    val currentWaste = items[currentItemIndex % items.size]

    // Al terminar los tiros, guardar en Firebase
    LaunchedEffect(isGameOver) {
        if (isGameOver && score > 0 && !isSavingScore) {
            isSavingScore = true
            val accuracy = (successfulHoops * 100L / 10L)
            scope.launch {
                scoreRepository.recordScore(
                    nickname = userProfile.nickname,
                    school = userProfile.school,
                    gameId = "encestar",
                    score = score,
                    itemsRecycled = successfulHoops.toLong(),
                    accuracy = accuracy
                ).fold(
                    onSuccess = {
                        profileRepository.incrementTotalScore(score)
                        isSavingScore = false
                    },
                    onFailure = {
                        isSavingScore = false
                    }
                )
            }
        }
    }

    fun shootHoop() {
        if (isShooting || isGameOver || shotsLeft <= 0) return

        isShooting = true
        val targetZone = 50f
        val aimDiff = abs(aimMeter.value - targetZone)
        val powerDiff = abs(powerSlider - 50f)
        val totalDeviation = aimDiff + powerDiff

        scope.launch {
            delay(600L) // Simulación del tiro en arco
            if (totalDeviation <= 25f) {
                // Tiro encestado
                val points = ((100 - totalDeviation.toInt()) * 2L).coerceAtLeast(60L)
                score += points
                successfulHoops++
                lastShotResult = "¡CANASTA PERFECTA! Encestaste ${currentWaste.name} en el contenedor ${currentWaste.category.containerColorName} (+${points} pts)"
            } else {
                lastShotResult = "¡Falló el tiro! Rozó el borde de la caneca. Ajusta la fuerza y el medidor de puntería."
            }

            shotsLeft--
            currentItemIndex++
            isShooting = false

            if (shotsLeft <= 0) {
                isGameOver = true
            }
        }
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
            // Barra superior
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
                    text = "Encestar Residuos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$score pts",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Marcador de tiros restantes
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tiros restantes: $shotsLeft / 10",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Aciertos: $successfulHoops",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (!isGameOver) {
                // Tarjeta del residuo a encestar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentWaste.iconEmoji,
                            fontSize = 56.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentWaste.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Caneca objetivo: ${currentWaste.category.containerColorName} (${currentWaste.category.title})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Canecas en la cancha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TargetCaneca(
                        name = "Blanco",
                        category = BinCategory.APROVECHABLE,
                        isTarget = currentWaste.category == BinCategory.APROVECHABLE,
                        color = Color(0xFFECEFF1),
                        textColor = Color.Black
                    )
                    TargetCaneca(
                        name = "Verde",
                        category = BinCategory.ORGANICO,
                        isTarget = currentWaste.category == BinCategory.ORGANICO,
                        color = Color(0xFF4CAF50),
                        textColor = Color.White
                    )
                    TargetCaneca(
                        name = "Negro",
                        category = BinCategory.NO_APROVECHABLE,
                        isTarget = currentWaste.category == BinCategory.NO_APROVECHABLE,
                        color = Color(0xFF212121),
                        textColor = Color.White
                    )
                }

                if (lastShotResult != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (lastShotResult?.contains("CANASTA") == true) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = lastShotResult ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp),
                            textAlign = TextAlign.Center,
                            color = if (lastShotResult?.contains("CANASTA") == true) Color(0xFF1B5E20) else Color(0xFFC62828)
                        )
                    }
                }

                // Controles de puntería y tiro
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Medidor oscilante de puntería
                        Text(
                            text = "Puntería de Tiro (Zona verde centrada)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.LightGray)
                        ) {
                            // Zona de acierto en el centro
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.3f)
                                    .height(24.dp)
                                    .align(Alignment.Center)
                                    .background(Color(0xFF81C784))
                            )
                            // Puntero en movimiento
                            Box(
                                modifier = Modifier
                                    .offset {
                                        val maxOffset = 250 // dp approx
                                        IntOffset((aimMeter.value * 2.5).roundToInt(), 0)
                                    }
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Control de fuerza
                        Text(
                            text = "Fuerza de Lanzamiento: ${powerSlider.roundToInt()}%",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Slider(
                            value = powerSlider,
                            onValueChange = { powerSlider = it },
                            valueRange = 0f..100f,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Botón de lanzamiento
                        Button(
                            onClick = { shootHoop() },
                            enabled = !isShooting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("shoot_button"),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.SportsBasketball, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isShooting) "Lanzando..." else "¡Lanzar Residuo!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // Pantalla de resultados finales
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
                            text = "¡Ronda de Encestar Finalizada!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Puntaje total: $score pts",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Canastas logradas: $successfulHoops de 10 tiros",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Puntería ecológica: ${(successfulHoops * 10)}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    shotsLeft = 10
                                    score = 0L
                                    successfulHoops = 0
                                    lastShotResult = null
                                    isGameOver = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Jugar de nuevo")
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
private fun TargetCaneca(
    name: String,
    category: BinCategory,
    isTarget: Boolean,
    color: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier
            .size(width = 96.dp, height = 110.dp)
            .border(
                width = if (isTarget) 3.dp else 1.dp,
                color = if (isTarget) MaterialTheme.colorScheme.primary else Color.Gray,
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = name,
                tint = textColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            if (isTarget) {
                Text(
                    text = "¡Objetivo!",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}
