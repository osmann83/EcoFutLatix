package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GameScore
import com.example.data.model.UserProfile
import com.example.data.repository.EcoScoreRepository

/**
 * Tabla de posiciones global en tiempo real (Leaderboard) conectada a Firebase Firestore.
 * Muestra el podio y la clasificación por colegios de Cali y minijuegos.
 */
@Composable
fun LeaderboardScreen(
    currentProfile: UserProfile,
    scoreRepository: EcoScoreRepository,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackToMenu() }

    var selectedGameTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "Basuraleza" to "basuraleza",
        "Tesoro" to "busqueda_tesoro",
        "Encestar" to "encestar"
    )

    val currentFilterGameId = tabs[selectedGameTab].second
    val scoresFlow = remember(currentFilterGameId) {
        scoreRepository.observeScoresByGame(currentFilterGameId)
    }
    val scoresList by scoresFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ranking Global",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Box(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selector de juego (Tabs)
            TabRow(
                selectedTabIndex = selectedGameTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                tabs.forEachIndexed { index, (title, _) ->
                    Tab(
                        selected = selectedGameTab == index,
                        onClick = { selectedGameTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedGameTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (scoresList.isEmpty()) {
                // Estado vacío amigable
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Sincronizando tabla en tiempo real con Firebase...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Podio Top 3 (si hay al menos un registro)
                val top3 = scoresList.take(3)
                if (top3.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // 2do Lugar (Plata)
                        if (top3.size >= 2) {
                            PodiumColumn(
                                score = top3[1],
                                rank = 2,
                                heightDp = 100,
                                medalColor = Color(0xFFC0C0C0),
                                isCurrent = top3[1].userId == currentProfile.userId
                            )
                        }

                        // 1er Lugar (Oro)
                        PodiumColumn(
                            score = top3[0],
                            rank = 1,
                            heightDp = 130,
                            medalColor = Color(0xFFFFD700),
                            isCurrent = top3[0].userId == currentProfile.userId
                        )

                        // 3er Lugar (Bronce)
                        if (top3.size >= 3) {
                            PodiumColumn(
                                score = top3[2],
                                rank = 3,
                                heightDp = 80,
                                medalColor = Color(0xFFCD7F32),
                                isCurrent = top3[2].userId == currentProfile.userId
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Lista de posiciones completa
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(scoresList) { index, item ->
                        val isCurrentUser = item.userId == currentProfile.userId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isCurrentUser) 2.dp else 0.dp,
                                    color = if (isCurrentUser) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .testTag("leaderboard_item_$index"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (index) {
                                                    0 -> Color(0xFFFFD700)
                                                    1 -> Color(0xFFC0C0C0)
                                                    2 -> Color(0xFFCD7F32)
                                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (index < 3) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.nickname,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (isCurrentUser) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "(Tú)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.School,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = item.school,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${item.score} pts",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${item.itemsRecycled} reciclados • ${item.accuracy}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PodiumColumn(
    score: GameScore,
    rank: Int,
    heightDp: Int,
    medalColor: Color,
    isCurrent: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(95.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(medalColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (rank) {
                    1 -> "🥇"
                    2 -> "🥈"
                    else -> "🥉"
                },
                fontSize = 18.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = score.nickname,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
        Text(
            text = "${score.score} pts",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(85.dp)
                .height(heightDp.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(
                    if (isCurrent) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$rank",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
