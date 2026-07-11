package com.example.torchtap

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import kotlin.math.roundToInt

/** Encapsula el encendido/apagado de la linterna vía Camera2. */
class TorchController(context: Context) {

    private val cameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    // Primer cámara que tenga unidad de flash.
    private val torchCameraId: String? = cameraManager.cameraIdList.firstOrNull { id ->
        cameraManager.getCameraCharacteristics(id)
            .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
    }
    private val maxStrengthLevel: Int = torchCameraId?.let { id ->
        cameraManager.getCameraCharacteristics(id)
            .get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
    } ?: 1

    val hasFlash: Boolean get() = torchCameraId != null

    /** true si el dispositivo permite ajustar el brillo del flash. */
    val supportsBrightness: Boolean get() = maxStrengthLevel > 1

    /** Convierte una fracción 0..1 en un nivel de brillo válido (1..max). */
    fun levelForFraction(fraction: Float): Int {
        val span = (maxStrengthLevel - 1).coerceAtLeast(0)
        return 1 + (fraction.coerceIn(0f, 1f) * span).roundToInt()
    }

    /**
     * Enciende/apaga la linterna. Si [enabled] es true y el dispositivo soporta
     * brillo, la enciende al nivel [level] (1..max).
     */
    fun setTorch(enabled: Boolean, level: Int = maxStrengthLevel) {
        val id = torchCameraId ?: return
        // runCatching: el flash puede estar ocupado por otra app.
        runCatching {
            if (enabled && supportsBrightness) {
                cameraManager.turnOnTorchWithStrengthLevel(
                    id,
                    level.coerceIn(1, maxStrengthLevel),
                )
            } else {
                cameraManager.setTorchMode(id, enabled)
            }
        }
    }
}
