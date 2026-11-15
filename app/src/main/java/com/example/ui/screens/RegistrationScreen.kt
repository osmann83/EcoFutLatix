package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.util.CaliSchools
import java.util.Calendar

/**
 * Pantalla de Formulario de Registro para estudiantes de 12 a 17 años de Cali.
 * Implementa las validaciones de Nickname, Institución Educativa y Fecha de Nacimiento
 * especificadas en los bocetos 'Formulario' y 'Error'.
 */
@Composable
fun RegistrationScreen(
    initialNickname: String,
    onRegister: (nickname: String, school: String, year: Int, month: Int, day: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var nickname by remember { mutableStateOf(initialNickname) }
    var selectedSchool by remember { mutableStateOf(CaliSchools.list.first()) }
    var schoolMenuExpanded by remember { mutableStateOf(false) }

    // Fecha de nacimiento predeterminada dentro del rango (ej. 14 años)
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    var birthYear by remember { mutableIntStateOf(currentYear - 14) }
    var birthMonth by remember { mutableIntStateOf(5) } // Mayo
    var birthDay by remember { mutableIntStateOf(15) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Rango permitido de años para 12 a 17 años
    val minAllowedYear = currentYear - 17
    val maxAllowedYear = currentYear - 12
    val yearsList = (minAllowedYear - 5..maxAllowedYear + 5).toList().reversed()
    var yearMenuExpanded by remember { mutableStateOf(false) }
    var monthMenuExpanded by remember { mutableStateOf(false) }
    var dayMenuExpanded by remember { mutableStateOf(false) }

    val monthsList = listOf(
        1 to "Enero", 2 to "Febrero", 3 to "Marzo", 4 to "Abril",
        5 to "Mayo", 6 to "Junio", 7 to "Julio", 8 to "Agosto",
        9 to "Septiembre", 10 to "Octubre", 11 to "Noviembre", 12 to "Diciembre"
    )

    fun calculateAge(y: Int, m: Int, d: Int): Int {
        val today = Calendar.getInstance()
        var calculated = today.get(Calendar.YEAR) - y
        if (today.get(Calendar.MONTH) + 1 < m ||
            (today.get(Calendar.MONTH) + 1 == m && today.get(Calendar.DAY_OF_MONTH) < d)
        ) {
            calculated--
        }
        return calculated
    }

    val currentAge = calculateAge(birthYear, birthMonth, birthDay)
    val isAgeValid = currentAge in 12..17

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabecera superior
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Recycling,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EcoFutLatix",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Registro de Estudiante",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Completa tu perfil para unirte a la competencia ecológica",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Campo 1: Nickname
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Ingresa su Nickname",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = nickname,
                    onValueChange = {
                        nickname = it
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nickname_input"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    placeholder = { Text("Ej. EcoGuerrero15") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Campo 2: Institución Pública (Colegios de Cali)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Institución pública",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { schoolMenuExpanded = true }
                            .testTag("school_selector"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = selectedSchool,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Desplegar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = schoolMenuExpanded,
                        onDismissRequest = { schoolMenuExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        CaliSchools.list.forEach { school ->
                            DropdownMenuItem(
                                text = { Text(school, style = MaterialTheme.typography.bodyMedium) },
                                onClick = {
                                    selectedSchool = school
                                    schoolMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Campo 3: Fecha de nacimiento y control de edad (12 a 17 años)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ingresa su fecha de nacimiento",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Edad: $currentAge años",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isAgeValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Selector de Día
                    Box(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { dayMenuExpanded = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Día", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    "$birthDay",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = dayMenuExpanded,
                            onDismissRequest = { dayMenuExpanded = false }
                        ) {
                            (1..31).forEach { day ->
                                DropdownMenuItem(
                                    text = { Text("$day") },
                                    onClick = {
                                        birthDay = day
                                        dayMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Selector de Mes
                    Box(modifier = Modifier.weight(1.4f)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { monthMenuExpanded = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Mes", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    monthsList.firstOrNull { it.first == birthMonth }?.second ?: "Mayo",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = monthMenuExpanded,
                            onDismissRequest = { monthMenuExpanded = false }
                        ) {
                            monthsList.forEach { (num, name) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        birthMonth = num
                                        monthMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Selector de Año
                    Box(modifier = Modifier.weight(1.2f)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { yearMenuExpanded = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAgeValid) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Año", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    "$birthYear",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = yearMenuExpanded,
                            onDismissRequest = { yearMenuExpanded = false }
                        ) {
                            yearsList.forEach { year ->
                                DropdownMenuItem(
                                    text = {
                                        val ageForYear = currentYear - year
                                        Text("$year ($ageForYear años)")
                                    },
                                    onClick = {
                                        birthYear = year
                                        yearMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Indicador de error (reproduce la pantalla 'Error' del boceto)
            if (!isAgeValid) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "¡¡Fecha fuera de límites!! La aplicación está enfocada únicamente en jóvenes de 12 a 17 años.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Botón de Enviar
            Button(
                onClick = {
                    if (nickname.trim().length < 3) {
                        errorMessage = "Nickname inválido. Mínimo 3 caracteres."
                        return@Button
                    }
                    if (!isAgeValid) {
                        errorMessage = "¡¡Fecha fuera de límites!! Debes tener entre 12 y 17 años."
                        return@Button
                    }
                    onRegister(nickname.trim(), selectedSchool, birthYear, birthMonth, birthDay)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("enviar_button"),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Enviar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
