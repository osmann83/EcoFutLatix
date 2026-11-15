package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import com.example.data.model.UserProfile
import com.example.data.repository.EcoScoreRepository
import com.example.data.repository.StudentProfileRepository
import kotlinx.coroutines.launch

enum class AppScreen {
    GAMES_MENU,
    BASURALEZA_GAME,
    TREASURE_HUNT_GAME,
    HOOP_GAME,
    LEADERBOARD
}

/**
 * Contenedor principal de la aplicación con Menú Lateral (Drawer) de navegación
 * basado en el boceto 'Menú Lateral' adjunto por el usuario.
 */
@Composable
fun MainAppScaffold(
    userProfile: UserProfile,
    scoreRepository: EcoScoreRepository,
    profileRepository: StudentProfileRepository,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(AppScreen.GAMES_MENU) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight(),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                // Cabecera del Drawer con información del estudiante
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = userProfile.nickname,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Estudiante (12-17 años)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = userProfile.school,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Puntaje total: ${userProfile.totalScore} pts",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Elementos de navegación del Menú Lateral (coincidiendo con el prototipo)
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Inicio") },
                    selected = currentScreen == AppScreen.GAMES_MENU,
                    onClick = {
                        currentScreen = AppScreen.GAMES_MENU
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null) },
                    label = { Text("Basuraleza") },
                    selected = currentScreen == AppScreen.BASURALEZA_GAME,
                    onClick = {
                        currentScreen = AppScreen.BASURALEZA_GAME
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Explore, contentDescription = null) },
                    label = { Text("Búsqueda Tesoro") },
                    selected = currentScreen == AppScreen.TREASURE_HUNT_GAME,
                    onClick = {
                        currentScreen = AppScreen.TREASURE_HUNT_GAME
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.SportsBasketball, contentDescription = null) },
                    label = { Text("Encestar") },
                    selected = currentScreen == AppScreen.HOOP_GAME,
                    onClick = {
                        currentScreen = AppScreen.HOOP_GAME
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
                    label = { Text("Tabla de Posiciones") },
                    selected = currentScreen == AppScreen.LEADERBOARD,
                    onClick = {
                        currentScreen = AppScreen.LEADERBOARD
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(modifier = Modifier.weight(1f))

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                NavigationDrawerItem(
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    label = {
                        Text(
                            "Cerrar Sesión / Exit",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onSignOut()
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .padding(bottom = 16.dp)
                )
            }
        }
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentScreen) {
                AppScreen.GAMES_MENU -> {
                    GameSelectionScreen(
                        userProfile = userProfile,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onLaunchGame = { game ->
                            currentScreen = when (game) {
                                SelectedGame.BASURALEZA -> AppScreen.BASURALEZA_GAME
                                SelectedGame.BUSQUEDA_TESORO -> AppScreen.TREASURE_HUNT_GAME
                                SelectedGame.ENCESTAR -> AppScreen.HOOP_GAME
                            }
                        },
                        onOpenLeaderboard = { currentScreen = AppScreen.LEADERBOARD }
                    )
                }
                AppScreen.BASURALEZA_GAME -> {
                    BasuralezaGameScreen(
                        userProfile = userProfile,
                        scoreRepository = scoreRepository,
                        profileRepository = profileRepository,
                        onBackToMenu = { currentScreen = AppScreen.GAMES_MENU }
                    )
                }
                AppScreen.TREASURE_HUNT_GAME -> {
                    TreasureHuntGameScreen(
                        userProfile = userProfile,
                        scoreRepository = scoreRepository,
                        profileRepository = profileRepository,
                        onBackToMenu = { currentScreen = AppScreen.GAMES_MENU }
                    )
                }
                AppScreen.HOOP_GAME -> {
                    RecycleHoopGameScreen(
                        userProfile = userProfile,
                        scoreRepository = scoreRepository,
                        profileRepository = profileRepository,
                        onBackToMenu = { currentScreen = AppScreen.GAMES_MENU }
                    )
                }
                AppScreen.LEADERBOARD -> {
                    LeaderboardScreen(
                        currentProfile = userProfile,
                        scoreRepository = scoreRepository,
                        onBackToMenu = { currentScreen = AppScreen.GAMES_MENU }
                    )
                }
            }
        }
    }
}
