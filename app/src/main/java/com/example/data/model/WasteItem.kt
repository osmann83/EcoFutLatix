package com.example.data.model

import androidx.compose.ui.graphics.Color

/**
 * Categorías del código de colores de reciclaje en Colombia (Resolución 2184).
 */
enum class BinCategory(
    val title: String,
    val containerColorName: String,
    val colorValue: Color,
    val description: String
) {
    APROVECHABLE(
        title = "Aprovechables",
        containerColorName = "Blanco",
        colorValue = Color(0xFFF5F5F5),
        description = "Plástico, vidrio, metales, papel y cartón limpios"
    ),
    ORGANICO(
        title = "Orgánicos",
        containerColorName = "Verde",
        colorValue = Color(0xFF4CAF50),
        description = "Restos de comida, cáscaras de fruta y residuos agrícolas"
    ),
    NO_APROVECHABLE(
        title = "No Aprovechables",
        containerColorName = "Negro",
        colorValue = Color(0xFF212121),
        description = "Papel higiénico, servilletas usadas, icopor y envolturas metalizadas"
    )
}

/**
 * Representa un residuo para los minijuegos pedagógicos.
 */
data class WasteItem(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val category: BinCategory,
    val ecoTip: String,
    val decompositionYears: String
)

/**
 * Catálogo educativo de residuos comunes en colegios y hogares de Cali.
 */
object WasteCatalogue {
    val items: List<WasteItem> = listOf(
        // Aprovechables (Blanco)
        WasteItem(
            id = "w1",
            name = "Botella de Plástico PET",
            iconEmoji = "🧴",
            category = BinCategory.APROVECHABLE,
            ecoTip = "Enjuágala y aplástala para ahorrar espacio en el reciclaje.",
            decompositionYears = "100 - 500 años"
        ),
        WasteItem(
            id = "w2",
            name = "Lata de Refresco (Aluminio)",
            iconEmoji = "🥫",
            category = BinCategory.APROVECHABLE,
            ecoTip = "El aluminio se puede reciclar infinitas veces sin perder calidad.",
            decompositionYears = "10 - 100 años"
        ),
        WasteItem(
            id = "w3",
            name = "Caja de Cartón Limpia",
            iconEmoji = "📦",
            category = BinCategory.APROVECHABLE,
            ecoTip = "Dóblala para facilitar su transporte a las plantas de reciclaje.",
            decompositionYears = "2 meses - 1 año"
        ),
        WasteItem(
            id = "w4",
            name = "Botella de Vidrio",
            iconEmoji = "🍾",
            category = BinCategory.APROVECHABLE,
            ecoTip = "El vidrio es 100% reciclable y ahorra hasta un 30% de energía.",
            decompositionYears = "Más de 4,000 años"
        ),
        WasteItem(
            id = "w5",
            name = "Cuaderno o Hojas Limpias",
            iconEmoji = "📄",
            category = BinCategory.APROVECHABLE,
            ecoTip = "Reciclar 1 tonelada de papel salva 17 árboles adultos.",
            decompositionYears = "1 año"
        ),

        // Orgánicos (Verde)
        WasteItem(
            id = "w6",
            name = "Cáscara de Plátano o Banano",
            iconEmoji = "🍌",
            category = BinCategory.ORGANICO,
            ecoTip = "Excelente abono orgánico (compost) rico en potasio para las plantas.",
            decompositionYears = "2 a 10 días"
        ),
        WasteItem(
            id = "w7",
            name = "Corazón de Manzana",
            iconEmoji = "🍎",
            category = BinCategory.ORGANICO,
            ecoTip = "Los residuos orgánicos nutren el suelo y evitan gases de efecto invernadero en vertederos.",
            decompositionYears = "2 meses"
        ),
        WasteItem(
            id = "w8",
            name = "Restos de Café o Té",
            iconEmoji = "☕",
            category = BinCategory.ORGANICO,
            ecoTip = "Se transforma rápidamente en tierra fértil para huertas escolares.",
            decompositionYears = "1 a 3 semanas"
        ),
        WasteItem(
            id = "w9",
            name = "Hojas y Ramas Secas",
            iconEmoji = "🍂",
            category = BinCategory.ORGANICO,
            ecoTip = "Materia vegetal seca que aporta carbono al compostaje.",
            decompositionYears = "1 a 6 meses"
        ),

        // No Aprovechables (Negro)
        WasteItem(
            id = "w10",
            name = "Servilleta con Grasa",
            iconEmoji = "🧻",
            category = BinCategory.NO_APROVECHABLE,
            ecoTip = "El papel con grasa o comida no se puede reciclar en el contenedor blanco.",
            decompositionYears = "Relleno sanitario"
        ),
        WasteItem(
            id = "w11",
            name = "Paquete de Papas (Metalizado)",
            iconEmoji = "🍟",
            category = BinCategory.NO_APROVECHABLE,
            ecoTip = "Las bolsas multicapa metalizadas no son reciclables tradicionalmente.",
            decompositionYears = "100 a 300 años"
        ),
        WasteItem(
            id = "w12",
            name = "Vaso o Plato de Icopor",
            iconEmoji = "🥤",
            category = BinCategory.NO_APROVECHABLE,
            ecoTip = "El poliestireno expandido contamina ríos y mares. ¡Evita los desechables!",
            decompositionYears = "500 a 1,000 años"
        )
    )
}
