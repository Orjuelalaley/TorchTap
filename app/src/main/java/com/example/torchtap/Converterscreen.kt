package com.example.torchtap

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * Categorías de conversión. Cada una define su par de unidades y el factor
 * "cuántas [unitB] hay en 1 [unitA]". Agrega más aquí en el futuro.
 */
private enum class Category(
    val label: String,
    val unitA: String,
    val unitB: String,
    val factor: Double,
) {
    WEIGHT("Peso", "lb", "kg", 0.45359237),
    LENGTH("Longitud", "cm", "in", 1.0 / 2.54),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(modifier: Modifier = Modifier) {
    var category by remember { mutableStateOf(Category.WEIGHT) }
    // false: unitA -> unitB. true: unitB -> unitA.
    var swapped by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }

    val fromUnit = if (swapped) category.unitB else category.unitA
    val toUnit = if (swapped) category.unitA else category.unitB

    // Gira el botón de invertir según la dirección activa.
    val swapRotation by animateFloatAsState(
        targetValue = if (swapped) 180f else 0f,
        animationSpec = tween(400),
        label = "swapRotation",
    )

    val result: String = remember(input, category, swapped) {
        val number = input.replace(',', '.').toDoubleOrNull()
        if (number == null) {
            ""
        } else {
            val converted = if (swapped) number / category.factor else number * category.factor
            String.format(Locale.US, "%.2f", converted)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        // Selector de categoría con botones segmentados.
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            Category.entries.forEachIndexed { index, cat ->
                SegmentedButton(
                    selected = cat == category,
                    onClick = {
                        category = cat
                        input = ""
                    },
                    shape = SegmentedButtonDefaults.itemShape(index, Category.entries.size),
                    icon = {
                        Icon(
                            imageVector = when (cat) {
                                Category.WEIGHT -> Icons.Filled.Scale
                                Category.LENGTH -> Icons.Filled.Straighten
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    label = { Text(cat.label) },
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        // Tarjeta de entrada.
        ConverterField(
            label = "De",
            unit = fromUnit,
            value = input,
            onValueChange = { new ->
                // Acepta solo dígitos y un separador decimal.
                if (new.isEmpty() || new.matches(Regex("^\\d*[.,]?\\d*$"))) {
                    input = new
                }
            },
        )

        Spacer(Modifier.height(12.dp))

        // Botón para invertir la dirección, centrado entre las dos tarjetas.
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            FilledIconButton(
                onClick = {
                    // Lleva el resultado al campo de entrada para "convertir de vuelta".
                    val carry = result
                    swapped = !swapped
                    input = carry
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier.size(52.dp),
            ) {
                Icon(
                    Icons.Filled.SwapVert,
                    contentDescription = "Invertir conversión",
                    modifier = Modifier.rotate(swapRotation),
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Tarjeta de resultado (solo lectura).
        ConverterField(
            label = "A",
            unit = toUnit,
            value = result,
            onValueChange = null,
        )
    }
}

/**
 * Tarjeta de una unidad. Si [onValueChange] es null, se muestra en modo solo
 * lectura (resultado); si no, es un campo editable con teclado decimal.
 */
@Composable
private fun ConverterField(
    label: String,
    unit: String,
    value: String,
    onValueChange: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val editable = onValueChange != null

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))

                val bigText = TextStyle(
                    fontSize = 34.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (value.isEmpty()) colorScheme.outline else colorScheme.onSurface,
                )
                if (editable) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = bigText,
                        singleLine = true,
                        cursorBrush = SolidColor(colorScheme.primary),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        decorationBox = { inner ->
                            if (value.isEmpty()) {
                                Text("0", style = bigText)
                            }
                            inner()
                        },
                    )
                } else {
                    Text(
                        text = value.ifEmpty { "0" },
                        style = bigText.copy(
                            color = if (value.isEmpty()) colorScheme.outline else colorScheme.primary,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Píldora con la unidad; anima el cambio al alternar categoría/dirección.
            Surface(
                color = colorScheme.primary,
                contentColor = colorScheme.onPrimary,
                shape = CircleShape,
            ) {
                AnimatedContent(
                    targetState = unit,
                    transitionSpec = {
                        (slideInVertically { it } + fadeIn()) togetherWith
                            (slideOutVertically { -it } + fadeOut())
                    },
                    label = "unit",
                ) { u ->
                    Text(
                        text = u,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}
