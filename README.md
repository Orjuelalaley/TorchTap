# TorchTap

Una app de linterna minimalista para Android, construida con **Jetpack Compose**. Un
botón grande para encender/apagar el flash, control de brillo cuando el dispositivo lo
soporta, y un banner de AdMob.

## Características

- 🔦 Encendido/apagado del flash mediante la API **Camera2** (`CameraManager`).
- 🌗 Control de **brillo** del flash en dispositivos compatibles
  (`FLASH_INFO_STRENGTH_MAXIMUM_LEVEL > 1`).
- 🎨 UI con Jetpack Compose y Material 3, con animaciones de color y glow.
- 🛡️ El flash se apaga automáticamente al cerrar la app.
- 📱 Banner de Google AdMob en la parte inferior.

## Requisitos

- Android Studio reciente (con Android Gradle Plugin 9.x).
- **minSdk 35**, **targetSdk / compileSdk 36**.
- Un dispositivo o emulador con flash para probar la linterna.

## Compilar y ejecutar

Usa el wrapper de Gradle (`./gradlew` en Unix, `gradlew.bat` en Windows):

```bash
# Compilar APK de debug
./gradlew assembleDebug

# Instalar en un dispositivo/emulador conectado
./gradlew installDebug

# Tests unitarios (JVM)
./gradlew testDebugUnitTest

# Tests instrumentados (requieren dispositivo/emulador)
./gradlew connectedDebugAndroidTest

# Lint
./gradlew lint
```

## Estructura

El código vive en `app/src/main/java/com/example/torchtap/`:

| Archivo | Responsabilidad |
| --- | --- |
| `TorchController.kt` | Única capa que habla con el hardware (Camera2): detecta el flash, lo enciende/apaga y mapea el brillo. |
| `MainActivity.kt` | Posee el `TorchController` y la UI Compose (`FlashlightScreen`, `PowerButton`). |
| `AdmobBanner.kt` | Wrapper Compose del `AdView` de AdMob. |

## AdMob

> ⚠️ Las unidades de anuncios actuales son las de **prueba** de Google.
> Reemplaza `BANNER_AD_UNIT_ID` en `AdmobBanner.kt` y el Application ID en
> `AndroidManifest.xml` por los tuyos antes de publicar a producción.

## Licencia

Sin licencia definida aún.