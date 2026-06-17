# Mi Estacionamiento â€” AplicaciÃ³n Android

> Plataforma mÃ³vil para la reserva y gestiÃ³n de estacionamientos en Chile.
> Conecta a conductores que buscan estacionamiento con propietarios que ofrecen espacios disponibles.

**VersiÃ³n actual:** 1.8.0 Â· **Android mÃ­nimo:** 7.0 (API 24) Â· **Lenguaje:** Kotlin

---

## Estructura del repositorio

```
MiEstacionamiento/
â”‚
â”œâ”€â”€ Documentacion/
â”‚   â”œâ”€â”€ Diagrama/              â† Diagramas UML y de arquitectura
â”‚   â”œâ”€â”€ BDD/                   â† DocumentaciÃ³n de base de datos
â”‚   â””â”€â”€ MyParking/             â† DocumentaciÃ³n especÃ­fica del producto
â”‚
â”œâ”€â”€ Producto/
â”‚   â””â”€â”€ My Parking/            â† CÃ³digo fuente del proyecto Android
â”‚       â”œâ”€â”€ app/
â”‚       â”‚   â”œâ”€â”€ google-services.json   â† Firebase (no se sube al repositorio)
â”‚       â”‚   â””â”€â”€ src/main/
â”‚       â”‚       â”œâ”€â”€ java/com/miestacionamiento/
â”‚       â”‚       â”‚   â”œâ”€â”€ data/          â† Modelos, Room, Retrofit, Repository
â”‚       â”‚       â”‚   â”œâ”€â”€ service/       â† Firebase Messaging Service
â”‚       â”‚       â”‚   â”œâ”€â”€ ui/            â† Activities y Fragments (MVVM)
â”‚       â”‚       â”‚   â””â”€â”€ utils/         â† Extensions, Preferences, Notificaciones
â”‚       â”‚       â””â”€â”€ res/               â† Layouts, drawables, navegaciÃ³n, strings
â”‚       â”œâ”€â”€ build.gradle
â”‚       â””â”€â”€ local.properties           â† API Keys locales (no se sube al repositorio)
â”‚
â”œâ”€â”€ Gestion/
â”‚   â”œâ”€â”€ 1.1.2 Documento de registro de definiciÃ³n e identificaciÃ³n del proyecto.docx
â”‚   â””â”€â”€ Integrantes.txt
â”‚
â””â”€â”€ README.md
```

---

## ConfiguraciÃ³n del entorno

### Requisitos previos
- Android Studio Hedgehog o superior
- JDK 17
- Dispositivo o emulador con Android 7.0+ (API 24)

### Pasos para ejecutar el proyecto

1. Clona el repositorio y abre Android Studio â†’ **File â†’ Open** â†’ selecciona la carpeta `MiEstacionamiento`
2. Espera que Gradle sincronice las dependencias (primera vez descarga ~400 MB)
3. Agrega tu Google Maps API Key en `Producto/My Parking/local.properties`:
   ```
   MAPS_API_KEY=TU_CLAVE_AQUI
   ```
   ObtÃ©n una clave gratuita en [Google Cloud Console](https://console.cloud.google.com) â†’ APIs â†’ Maps SDK for Android
4. Descarga el archivo `google-services.json` desde [Firebase Console](https://console.firebase.google.com) â†’ tu proyecto â†’ ConfiguraciÃ³n â†’ App Android, y colÃ³calo en `Producto/My Parking/app/`
5. Ejecuta la app en un emulador o dispositivo fÃ­sico

> **Nota:** Los archivos `google-services.json` y `local.properties` estÃ¡n excluidos del repositorio por seguridad. Cada desarrollador debe obtener su propia copia.

---

## Funcionalidades por pantalla

| MÃ³dulo | Funcionalidades |
|--------|----------------|
| **Login** | Inicio de sesiÃ³n con email y contraseÃ±a, sesiÃ³n persistente (no pide login al reabrir), recuperaciÃ³n de contraseÃ±a por email, acceso con Google |
| **Registro** | SelecciÃ³n de tipo de usuario (Conductor / Propietario), formulario dinÃ¡mico segÃºn rol, carga de foto de perfil desde galerÃ­a o cÃ¡mara |
| **Home (Conductor)** | Estacionamientos populares en cards horizontales, vistos recientemente, bÃºsqueda rÃ¡pida |
| **Explorar** | BÃºsqueda en tiempo real, mapa interactivo con Google Maps, lista vertical con opciÃ³n de guardar |
| **Detalle** | InformaciÃ³n completa del estacionamiento, mapa, disponibilidad, reseÃ±as, iniciar chat con propietario, reservar |
| **Reserva y Pago** | SelecciÃ³n de horas, pago integrado con Flow (sistema de pagos chileno), confirmaciÃ³n por polling |
| **Historial** | Lista de reservas con filtros (Todas / Completadas / Pendientes / Fallidas), estadÃ­sticas de gasto |
| **Guardados** | Grilla de estacionamientos favoritos con cachÃ© local (Room Database) |
| **Chat** | Conversaciones en tiempo real entre conductores y propietarios, lista de conversaciones con contador de no leÃ­dos |
| **Perfil** | EdiciÃ³n de datos personales, informaciÃ³n de vehÃ­culo (conductor), datos de ubicaciÃ³n (propietario), cerrar sesiÃ³n |
| **Dashboard (Propietario)** | Resumen de ingresos mensuales, reservas activas, conductores Ãºnicos |
| **Mis Estacionamientos** | Crear, editar, eliminar y activar/desactivar estacionamientos con imagen |
| **EstadÃ­sticas** | GrÃ¡ficos mensuales de ingresos y reservas con MPAndroidChart |
| **ReseÃ±as** | Ver y responder reseÃ±as de los conductores |
| **Notificaciones Push** | Reserva confirmada, pago recibido, nuevo mensaje, nueva reseÃ±a, tiempo por vencer, estacionamiento lleno, entre otras |

---

## TecnologÃ­as utilizadas

| CategorÃ­a | TecnologÃ­a |
|-----------|-----------|
| Lenguaje | Kotlin |
| Arquitectura | MVVM + Repository Pattern |
| UI | Material Design 3, View Binding, Navigation Component |
| Base de datos local | Room Database |
| Red | Retrofit 2 + OkHttp + Gson |
| Notificaciones | Firebase Cloud Messaging (FCM) |
| Mapas | Google Maps SDK + Location Services |
| SesiÃ³n | DataStore Preferences |
| ImÃ¡genes | Glide |
| GrÃ¡ficos | MPAndroidChart |
| Pagos | Flow (integraciÃ³n con Chrome Custom Tabs) |
| Backend | Node.js en Oracle Cloud Â· `http://161.153.192.177:3000/api/` |

---

## Historial de versiones

| VersiÃ³n | DescripciÃ³n |
|---------|-------------|
| **1.8.0** | Notificaciones push FCM (9 tipos), sesiÃ³n persistente, pantalla de recuperar contraseÃ±a, eliminaciÃ³n de login con Facebook, correcciones de navegaciÃ³n desde notificaciones |
| **1.0.0** | VersiÃ³n inicial: autenticaciÃ³n, explorar estacionamientos, detalle con mapa, guardados y perfil bÃ¡sico |

---

## Notas de los desarrolladores

### Mejoras pendientes para prÃ³ximas versiones

- Completar inicio de sesiÃ³n con Google (requiere configurar SHA-1 en Firebase Console y habilitar Google Sign-In en Firebase Auth)
- Implementar endpoint `POST /auth/forgot-password` en el backend para envÃ­o real de emails de recuperaciÃ³n
- Agregar funcionalidad del botÃ³n "Editar Perfil"
- Agregar botÃ³n de aceptar tÃ©rminos y condiciones en el registro

### Bugs conocidos

- El modo oscuro no cambia el tema correctamente en todas las pantallas
- El mapa en la pantalla de exploraciÃ³n presenta fallos de carga intermitentes
