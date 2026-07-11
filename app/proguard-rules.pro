# Reglas ProGuard/R8 para el build de release.
# Compose, AdMob (play-services-ads) y Camera2 traen sus propias reglas
# "consumer" en sus librerías, así que aquí solo van ajustes propios del proyecto.

# Conserva la información de líneas para stacktraces legibles en Crashlytics/Play Console,
# pero oculta el nombre del archivo fuente original.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
