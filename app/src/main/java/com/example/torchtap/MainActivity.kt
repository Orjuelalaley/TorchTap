package com.example.torchtap

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    private lateinit var torch: TorchController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        torch = TorchController(applicationContext)

        // Marca este teléfono como dispositivo de prueba: así tus propias impresiones/clics
        // sobre el ad unit de PRODUCCIÓN no cuentan como tráfico real y evitas un baneo.
        // Para registrar otro dispositivo, agrega su hash a la lista.
        val testConfig = RequestConfiguration.Builder()
            .setTestDeviceIds(listOf("22FAFEE2D69504536384340A1DBE44CF"))
            .build()
        MobileAds.setRequestConfiguration(testConfig)

        // Inicializa el SDK de Mobile Ads una sola vez.
        MobileAds.initialize(this) {}

        setContent {
            // Tema oscuro por defecto; el botón de la barra superior lo alterna.
            var darkTheme by rememberSaveable { mutableStateOf(true) }

            // Ajusta el color de los iconos de la barra de estado al tema activo.
            val view = LocalView.current
            SideEffect {
                val window = (view.context as Activity).window
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }

            TorchTapTheme(darkTheme = darkTheme) {
                AppRoot(
                    hasFlash = torch.hasFlash,
                    supportsBrightness = torch.supportsBrightness,
                    darkTheme = darkTheme,
                    onToggleTheme = { darkTheme = !darkTheme },
                    onApply = { on, fraction ->
                        torch.setTorch(on, torch.levelForFraction(fraction))
                    },
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        torch.setTorch(false)
    }
}

/** Catálogo de herramientas. Agrega una nueva aquí y el menú la muestra solo. */
private enum class Tool(val label: String, val icon: ImageVector) {
    TORCH("Linterna", Icons.Filled.FlashlightOn),
    CONVERTER("Conversor", Icons.Filled.SwapHoriz),
    // FUTURO: TIP("Propinas", Icons.Filled.Info),
    // FUTURO: TIMER("Temporizador", Icons.Filled.DateRange),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(
    hasFlash: Boolean,
    supportsBrightness: Boolean,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onApply: (on: Boolean, fraction: Float) -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedTool by rememberSaveable { mutableStateOf(Tool.TORCH) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(28.dp)) {
                    // Logo del header. Cuando tengas tu ícono propio,
                    // cámbialo por R.drawable.ic_torchtap.
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("TorchTap", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Herramientas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                Tool.entries.forEach { tool ->
                    NavigationDrawerItem(
                        icon = { Icon(tool.icon, contentDescription = null) },
                        label = { Text(tool.label) },
                        selected = tool == selectedTool,
                        onClick = {
                            selectedTool = tool
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(selectedTool.label) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Abrir menú")
                        }
                    },
                    actions = {
                        // Alterna entre modo oscuro y claro.
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                imageVector = if (darkTheme) Icons.Filled.LightMode
                                else Icons.Filled.DarkMode,
                                contentDescription = if (darkTheme) "Cambiar a modo claro"
                                else "Cambiar a modo oscuro",
                            )
                        }
                    },
                )
            },
            bottomBar = {
                AdmobBanner(modifier = Modifier.fillMaxWidth())
            },
        ) { padding ->
            when (selectedTool) {
                Tool.TORCH -> TorchContent(
                    modifier = Modifier.padding(padding),
                    hasFlash = hasFlash,
                    supportsBrightness = supportsBrightness,
                    darkTheme = darkTheme,
                    onApply = onApply,
                )
                Tool.CONVERTER -> ConverterScreen(modifier = Modifier.padding(padding))
            }
        }
    }
}

@Composable
fun TorchContent(
    modifier: Modifier = Modifier,
    hasFlash: Boolean,
    supportsBrightness: Boolean,
    darkTheme: Boolean,
    onApply: (on: Boolean, fraction: Float) -> Unit,
) {
    var isOn by rememberSaveable { mutableStateOf(false) }
    var brightness by rememberSaveable { mutableFloatStateOf(1f) }

    val colorScheme = MaterialTheme.colorScheme

    // Fondo cálido cuando la linterna está encendida; sigue el tema cuando está apagada.
    val warmOn = if (darkTheme) Color(0xFF15120A) else Color(0xFFFFF3D6)
    val background by animateColorAsState(
        targetValue = if (isOn) warmOn else colorScheme.background,
        animationSpec = tween(400),
        label = "background",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (!hasFlash) {
                Text(
                    text = "Este dispositivo no tiene linterna.",
                    color = colorScheme.onSurfaceVariant,
                )
                return@Column
            }

            Text(
                text = if (isOn) "ON" else "OFF",
                color = if (isOn) colorScheme.primary else colorScheme.onSurfaceVariant,
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
                color = colorScheme.onSurfaceVariant,
                fontSize = 15.sp,
            )

            // Control de brillo: solo si el dispositivo lo soporta.
            if (supportsBrightness) {
                Spacer(Modifier.height(32.dp))
                Text(
                    text = "Brillo  ${(brightness * 100).roundToInt()}%",
                    color = if (isOn) colorScheme.primary else colorScheme.onSurfaceVariant,
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
                        thumbColor = colorScheme.primary,
                        activeTrackColor = colorScheme.primary,
                        inactiveTrackColor = colorScheme.surfaceVariant,
                        disabledThumbColor = colorScheme.outline,
                        disabledActiveTrackColor = colorScheme.outline,
                        disabledInactiveTrackColor = colorScheme.surfaceVariant,
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
    val colorScheme = MaterialTheme.colorScheme

    // Encendido: siempre ámbar (identidad de marca). Apagado: sigue el tema.
    val topColor by animateColorAsState(
        targetValue = if (isOn) Color(0xFFFFE082) else colorScheme.surfaceVariant,
        animationSpec = tween(400),
        label = "top",
    )
    val bottomColor by animateColorAsState(
        targetValue = if (isOn) Color(0xFFFFB300) else colorScheme.surface,
        animationSpec = tween(400),
        label = "bottom",
    )
    val iconColor by animateColorAsState(
        targetValue = if (isOn) Color(0xFF1A1A1A) else colorScheme.onSurfaceVariant,
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
