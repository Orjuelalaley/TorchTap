package com.example.torchtap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.ads.MobileAds
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    private lateinit var torch: TorchController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        torch = TorchController(applicationContext)
        MobileAds.initialize(this) {}
        setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    FlashlightScreen(
                        hasFlash = torch.hasFlash,
                        supportsBrightness = torch.supportsBrightness,
                        onApply = { on, fraction ->
                            torch.setTorch(on, torch.levelForFraction(fraction))
                        },
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        torch.setTorch(false)
    }
}

@Composable
fun FlashlightScreen(
    hasFlash: Boolean,
    supportsBrightness: Boolean,
    onApply: (on: Boolean, fraction: Float) -> Unit,
) {
    var isOn by rememberSaveable { mutableStateOf(false) }
    var brightness by rememberSaveable { mutableStateOf(1f) }

    val background by animateColorAsState(
        targetValue = if (isOn) Color(0xFF15120A) else Color(0xFF0E1116),
        animationSpec = tween(400),
        label = "background",
    )

    Scaffold(
        containerColor = background,
        bottomBar = {
            // Banner estándar compacto, fuera del camino.
            AdmobBanner(modifier = Modifier.fillMaxWidth())
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (!hasFlash) {
                Text(
                    text = "Este dispositivo no tiene linterna.",
                    color = Color(0xFFB0B6C0),
                )
                return@Column
            }

            Text(
                text = if (isOn) "ON" else "OFF",
                color = if (isOn) Color(0xFFFFC107) else Color(0xFF6B7280),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(40.dp))

            PowerButton(
                isOn = isOn,
                onClick = {
                    isOn = !isOn
                    onApply(isOn, brightness)
                },
            )

            Spacer(Modifier.height(40.dp))
            Text(
                text = if (isOn) "Toca para apagar" else "Toca para encender",
                color = Color(0xFF8A909C),
                fontSize = 15.sp,
            )

            // Control de brillo: solo si el dispositivo lo soporta.
            if (supportsBrightness) {
                Spacer(Modifier.height(32.dp))
                Text(
                    text = "Brillo  ${(brightness * 100).roundToInt()}%",
                    color = if (isOn) Color(0xFFFFC107) else Color(0xFF6B7280),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
                Slider(
                    value = brightness,
                    onValueChange = { value ->
                        brightness = value
                        // Reajusta en vivo si la linterna ya está encendida.
                        if (isOn) onApply(true, value)
                    },
                    enabled = isOn,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFFB300),
                        activeTrackColor = Color(0xFFFFB300),
                        inactiveTrackColor = Color(0xFF2A2F38),
                        disabledThumbColor = Color(0xFF3A3F48),
                        disabledActiveTrackColor = Color(0xFF3A3F48),
                        disabledInactiveTrackColor = Color(0xFF22262E),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp),
                )
            }
        }
    }
}

@Composable
private fun PowerButton(
    isOn: Boolean,
    onClick: () -> Unit,
) {
    val topColor by animateColorAsState(
        targetValue = if (isOn) Color(0xFFFFE082) else Color(0xFF2A2F38),
        animationSpec = tween(400),
        label = "top",
    )
    val bottomColor by animateColorAsState(
        targetValue = if (isOn) Color(0xFFFFB300) else Color(0xFF1B1F26),
        animationSpec = tween(400),
        label = "bottom",
    )
    val iconColor by animateColorAsState(
        targetValue = if (isOn) Color(0xFF1A1A1A) else Color(0xFF6B7280),
        animationSpec = tween(400),
        label = "icon",
    )
    val glow by animateDpAsState(
        targetValue = if (isOn) 30.dp else 0.dp,
        animationSpec = tween(400),
        label = "glow",
    )

    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(190.dp)
            .shadow(
                elevation = glow,
                shape = CircleShape,
                spotColor = Color(0xFFFFB300),
                ambientColor = Color(0xFFFFB300),
            )
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(topColor, bottomColor)))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(80.dp)) {
            val stroke = size.minDimension * 0.09f
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = size.minDimension * 0.34f

            // Arco con hueco en la parte superior.
            drawArc(
                color = iconColor,
                startAngle = -65f,
                sweepAngle = 310f,
                useCenter = false,
                topLeft = Offset(cx - radius, cy - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            // Línea vertical superior del símbolo de encendido.
            drawLine(
                color = iconColor,
                start = Offset(cx, cy - radius * 1.25f),
                end = Offset(cx, cy - radius * 0.15f),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }
    }
}
