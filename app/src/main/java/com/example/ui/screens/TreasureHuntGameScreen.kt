package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.repository.EcoScoreRepository
import com.example.data.repository.StudentProfileRepository
import kotlinx.coroutines.launch

data class HiddenEcoItem(
    val id: String,
    val name: String,
    val emoji: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val funFact: String
)

/**
 * Minijuego 2: Búsqueda del Tesoro Ecológico.
 * Explora ecosistemas de Cali (Río Cali, Farallones, Entorno Escolar)
 * recolectando residuos contaminantes y respondiendo trivias ambientales.
 */
@Composable
fun TreasureHuntGameScreen(
    userProfile: UserProfile,
    scoreRepository: EcoScoreRepository,
    profileRepository: StudentProfileRepository,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackToMenu() }

    val scope = rememberCoroutineScope()
    var selectedScenarioIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableLongStateOf(0L) }
    var cleanedItemsCount by remember { mutableIntStateOf(0) }
    var activeTriviaItem by remember { mutableStateOf<HiddenEcoItem?>(null) }
    var triviaFeedback by remember { mutableStateOf<String?>(null) }
    var isSavingScore by remember { mutableStateOf(false) }
    var gameCompleted by remember { mutableStateOf(false) }

    val scenarios = listOf(
        "Riberas del Río Cali" to listOf(
            HiddenEcoItem(
                id = "rio_1",
                name = "Botella en el cauce",
                emoji = "🍾",
                question = "¿Cuánto tiempo tarda una botella plástica en fragmentarse en microplásticos?",
                options = listOf("1 a 2 años", "100 a 500 años", "1 semana"),
                correctIndex = 1,
                funFact = "El plástico en ríos afecta directamente a las aves y peces nativos de Cali."
            ),
            HiddenEcoItem(
                id = "rio_2",
                name = "Pila alcalina oxidada",
                emoji = "🔋",
                question = "¿Por qué una sola pila puede contaminar hasta 600,000 litros de agua?",
                options = listOf("Contiene mercurio y plomo pesados", "Es biodegradable", "Genera calor instantáneo"),
                correctIndex = 0,
                funFact = "Las pilas deben depositarse en puntos de recolección especializados (posconsumo)."
            ),
            HiddenEcoItem(
                id = "rio_3",
                name = "Bolsa plástica flotante",
                emoji = "🛍️",
                question = "¿Qué alternativa sostenible reemplaza mejor las bolsas desechables?",
                options = listOf("Bolsas de tela reutilizables", "Usar más plástico", "Arrojarlas al río"),
                correctIndex = 0,
                funFact = "Llevar tu propia bolsa de tela evita cientos de desechos al año."
            )
        ),
        "Parque Farallones" to listOf(
            HiddenEcoItem(
                id = "fara_1",
                name = "Lata de aluminio abandonada",
                emoji = "🥫",
                question = "¿Qué porcentaje de energía se ahorra al reciclar una lata de aluminio frente a fabricar una nueva?",
                options = listOf("Hasta un 95% de energía", "Menos del 5%", "Ninguna"),
                correctIndex = 0,
                funFact = "El aluminio es infinitamente reciclable en el contenedor blanco."
            ),
            HiddenEcoItem(
                id = "fara_2",
                name = "Envase de Icopor",
                emoji = "🥡",
                question = "¿Por qué el icopor es tan dañino en áreas protegidas como Farallones?",
                options = listOf("Se disuelve con agua", "No se biodegrada y fragmenta dañando animales", "Fertiliza árboles"),
                correctIndex = 1,
                funFact = "El icopor va a la caneca negra (no aprovechables)."
            ),
            HiddenEcoItem(
                id = "fara_3",
                name = "Colilla de cigarrillo",
                emoji = "🚬",
                question = "¿Qué contiene el filtro de una colilla que intoxica el suelo?",
                options = listOf("Acetato de celulosa y tóxicos químicos", "Azúcar", "Semillas"),
                correctIndex = 0,
                funFact = "Una sola colilla contamina hasta 50 litros de agua dulce."
            )
        ),
        "Patio Escolar" to listOf(
            HiddenEcoItem(
                id = "patio_1",
                name = "Papel de cuaderno rayado",
                emoji = "📄",
                question = "¿En qué caneca de color debemos depositar hojas y cuadernos limpios en el colegio?",
                options = listOf("Verde (Orgánicos)", "Negra (No aprovechables)", "Blanca (Aprovechables)"),
                correctIndex = 2,
                funFact = "En Colombia el código unificado de colores asigna el Blanco a papel y cartón limpio."
            ),
            HiddenEcoItem(
                id = "patio_2",
                name = "Cáscara de fruta del refrigerio",
                emoji = "🍊",
                question = "¿Cómo beneficia la cáscara de naranja o banano a la huerta escolar?",
                options = listOf("Mediante compostaje y nutrientes orgánicos", "Generando plástico", "Atrayendo plagas"),
                correctIndex = 0,
                funFact = "Los residuos orgánicos en la caneca verde crean abono natural."
            )
        )
    )

    // Conjunto de IDs recolectados
    var collectedItemIds by remember { mutableStateOf(setOf<String>()) }
    val totalItemsInGame = scenarios.sumOf { it.second.size }

    // Guardar puntaje en Firebase al completar
    LaunchedEffect(gameCompleted) {
        if (gameCompleted && score > 0 && !isSavingScore) {
            isSavingScore = true
            scope.launch {
                scoreRepository.recordScore(
                    nickname = userProfile.nickname,
                    school = userProfile.school,
                    gameId = "busqueda_tesoro",
                    score = score,
                    itemsRecycled = collectedItemIds.size.toLong(),
                    accuracy = 100L
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

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                    text = "Búsqueda del Tesoro",
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

            Spacer(modifier = Modifier.height(12.dp))

            // Selector de escenarios de Cali
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(scenarios.indices.toList()) { index ->
                    val isSelected = selectedScenarioIndex == index
                    val scenarioName = scenarios[index].first
                    Card(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedScenarioIndex = index }
                            .testTag("scenario_tab_$index"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (index == 0) Icons.Default.Water else Icons.Default.Nature,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = scenarioName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progreso total de descontaminación
            val progress = collectedItemIds.size.toFloat() / totalItemsInGame.toFloat()
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Limpieza ecológica: ${collectedItemIds.size}/$totalItemsInGame residuos",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Escenario visual interactivo
            val currentScenario = scenarios[selectedScenarioIndex]
            val itemsInCurrentScenario = currentScenario.second

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentScenario.first,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Toca cada residuo contaminante para resolver el desafío y restaurar el área",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Rejilla de residuos ocultos en el escenario
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsInCurrentScenario.forEach { item ->
                            val isCollected = collectedItemIds.contains(item.id)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(
                                        width = if (isCollected) 2.dp else 1.dp,
                                        color = if (isCollected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable(enabled = !isCollected) {
                                        activeTriviaItem = item
                                        triviaFeedback = null
                                    }
                                    .testTag("treasure_item_${item.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCollected) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (isCollected) "🌿" else item.emoji,
                                            fontSize = 32.sp
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = if (isCollected) "${item.name} (¡Rescatado!)" else item.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCollected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (isCollected) "Área restaurada y descontaminada" else "Toca para investigar impacto",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (isCollected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Completado",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Explore,
                                            contentDescription = "Examinar",
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (collectedItemIds.size == totalItemsInGame && !gameCompleted) {
                gameCompleted = true
            }

            if (gameCompleted) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¡Felicitaciones! Has completado la Búsqueda del Tesoro",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Puntaje total: $score pts guardado en Firebase.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Modal de Trivia y Resolución Ecológica
    if (activeTriviaItem != null) {
        val trivia = activeTriviaItem!!
        AlertDialog(
            onDismissRequest = { activeTriviaItem = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(trivia.emoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = trivia.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = trivia.question,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    trivia.options.forEachIndexed { optIndex, optText ->
                        Button(
                            onClick = {
                                if (optIndex == trivia.correctIndex) {
                                    score += 100L
                                    collectedItemIds = collectedItemIds + trivia.id
                                    activeTriviaItem = null
                                } else {
                                    triviaFeedback = "Intenta de nuevo: Reflexiona en la respuesta más ecológica."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(
                                text = optText,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    if (triviaFeedback != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = triviaFeedback ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                Button(
                    onClick = { activeTriviaItem = null },
                    colors = ButtonDefaults.textButtonColors()
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}
