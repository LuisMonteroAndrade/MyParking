# My Parking - Aplicacion Android

> Plataforma movil para la reserva y gestion de estacionamientos en Chile.
> Conecta a conductores que buscan estacionamiento con propietarios que ofrecen espacios disponibles.

**Version actual:** 1.8.0 | **Android minimo:** 7.0 (API 24) | **Lenguaje:** Kotlin

---

## Estructura del repositorio

```
MyParking/
|
+-- Documentacion/
|   +-- Diagrama/              <- Diagramas UML y de arquitectura
|   +-- BDD/                   <- Documentacion de base de datos
|   +-- MyParking/             <- Documentacion especifica del producto
|
+-- Producto/
|   +-- My Parking/            <- Codigo fuente del proyecto Android
|       +-- app/
|       |   +-- google-services.json   <- Firebase (no se sube al repositorio)
|       |   +-- src/main/
|       |       +-- java/com/miestacionamiento/
|       |       |   +-- data/          <- Modelos, Room, Retrofit, Repository
|       |       |   +-- service/       <- Firebase Messaging Service
|       |       |   +-- ui/            <- Activities y Fragments (MVVM)
|       |       |   +-- utils/         <- Extensions, Preferences, Notificaciones
|       |       +-- res/               <- Layouts, drawables, navegacion, strings
|       +-- build.gradle
|       +-- local.properties           <- API Keys locales (no se sube al repositorio)
|
+-- Gestion/
|   +-- Integrantes.txt
|
+-- README.md
```

---

## Configuracion del entorno

### Requisitos previos
- Android Studio Hedgehog o superior
- JDK 17
- Dispositivo o emulador con Android 7.0+ (API 24)

### Pasos para ejecutar el proyecto

1. Clona el repositorio y abre Android Studio -> **File -> Open** -> selecciona la carpeta `MiEstacionamiento`
2. Espera que Gradle sincronice las dependencias (primera vez descarga ~400 MB)
3. Agrega tu Google Maps API Key en `Producto/My Parking/local.properties`:
   ```
   MAPS_API_KEY=TU_CLAVE_AQUI
   ```
   Obten la clave en [Google Cloud Console](https://console.cloud.google.com) -> APIs -> Maps SDK for Android
4. Descarga el archivo `google-services.json` desde [Firebase Console](https://console.firebase.google.com) -> tu proyecto -> Configuracion -> App Android, y colocalo en `Producto/My Parking/app/`
5. Ejecuta la app en un emulador o dispositivo fisico

> **Nota:** Los archivos `google-services.json` y `local.properties` estan excluidos del repositorio por seguridad. Cada desarrollador debe obtener su propia copia.

---

## Funcionalidades por pantalla

| Modulo | Funcionalidades |
|--------|----------------|
| **Login** | Inicio de sesion con email y contrasena, sesion persistente (no pide login al reabrir), recuperacion de contrasena por email, acceso con Google |
| **Registro** | Seleccion de tipo de usuario (Conductor / Propietario), formulario dinamico segun rol, carga de foto de perfil desde galeria o camara |
| **Home (Conductor)** | Estacionamientos populares en cards horizontales, vistos recientemente, busqueda rapida |
| **Explorar** | Busqueda en tiempo real, mapa interactivo con Google Maps, lista vertical con opcion de guardar |
| **Detalle** | Informacion completa del estacionamiento, mapa, disponibilidad, resenas, iniciar chat con propietario, reservar |
| **Reserva y Pago** | Seleccion de horas, pago integrado con Flow (sistema de pagos chileno), confirmacion por polling |
| **Historial** | Lista de reservas con filtros (Todas / Completadas / Pendientes / Fallidas), estadisticas de gasto |
| **Guardados** | Grilla de estacionamientos favoritos con cache local (Room Database) |
| **Chat** | Conversaciones entre conductores y propietarios, lista de conversaciones con contador de no leidos |
| **Perfil** | Datos personales, informacion de vehiculo (conductor), datos de ubicacion (propietario), cerrar sesion |
| **Dashboard (Propietario)** | Resumen de ingresos mensuales, reservas activas, conductores unicos |
| **Mis Estacionamientos** | Crear, editar, eliminar y activar/desactivar estacionamientos con imagen |
| **Estadisticas** | Graficos mensuales de ingresos y reservas con MPAndroidChart |
| **Resenas** | Ver y responder resenas de los conductores |
| **Notificaciones Push** | Reserva confirmada, pago recibido, nuevo mensaje, nueva resena, tiempo por vencer, estacionamiento lleno, entre otras |

---

## Tecnologias utilizadas

| Categoria | Tecnologia |
|-----------|-----------|
| Lenguaje | Kotlin |
| Arquitectura | MVVM + Repository Pattern |
| UI | Material Design 3, View Binding, Navigation Component |
| Base de datos local | Room Database |
| Red | Retrofit 2 + OkHttp + Gson |
| Notificaciones | Firebase Cloud Messaging (FCM) |
| Mapas | Google Maps SDK + Location Services |
| Sesion | DataStore Preferences |
| Imagenes | Glide |
| Graficos | MPAndroidChart |
| Pagos | Flow (integracion con Chrome Custom Tabs) |
| Backend | Node.js en Oracle Cloud - `http://161.153.192.177:3000/api/` |

---

## Historial de versiones

| Version | Descripcion |
|---------|-------------|
| **1.8.0** | Notificaciones push FCM (9 tipos), sesion persistente, pantalla de recuperar contrasena, eliminacion de login con Facebook, correcciones de navegacion desde notificaciones |

---

## Notas de los desarrolladores

### Mejoras pendientes para proximas versiones

- Completar inicio de sesion con Google (requiere configurar SHA-1 en Firebase Console y habilitar Google Sign-In en Firebase Auth)
- Implementar endpoint `POST /auth/forgot-password` en el backend para envio real de emails de recuperacion
- Agregar funcionalidad del boton "Editar Perfil"
- Agregar boton de aceptar terminos y condiciones en el registro
