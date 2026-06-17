# Mi Estacionamiento — App Android

Aplicacion movil para reserva de estacionamientos en Chile.
Permite a conductores encontrar y reservar estacionamientos, y a propietarios gestionar sus espacios.

Version actual: 1.8.0

---

## Estructura del proyecto

MiEstacionamiento/
|-- settings.gradle / build.gradle / gradle.properties
|-- local.properties <- Aqui pones tu API Key de Google Maps
`-- app/
      |-- build.gradle
      |-- google-services.json  <- Necesario para Firebase (no se sube al repo)
      `-- src/main/
|-- AndroidManifest.xml
`-- java/com/miestacionamiento/
              |-- MiEstacionamientoApp.kt
              |-- data/
              |   |-- model/       Parking.kt, User.kt, AuthModels, ChatModels, etc.
              |   |-- local/       AppDatabase, ParkingDao, ParkingEntity
              |   |-- remote/      ApiService (Retrofit), RetrofitClient, AuthInterceptor
              |   `-- repository/ ParkingRepository
|-- service/
| `-- MyFirebaseMessagingService.kt
              |-- ui/
              |   |-- auth/        Login, Register, ForgotPassword (Activity + VM)
              |   |-- main/        MainActivity (BottomNav + NavController)
              |   |-- home/        HomeFragment + VM + Adapter
              |   |-- explore/     ExploreFragment + VM + ParkingAdapter
              |   |-- detail/      DetailFragment + VM (Maps + reservas + pago Flow)
              |   |-- saved/       SavedFragment + VM
              |   |-- chat/        ChatListFragment, ChatFragment + VM
              |   |-- history/     BookingHistoryFragment + VM
              |   |-- profile/     ProfileFragment + VM
              |   |-- owner/       Dashboard, MyParkings, Stats, ParkingForm, Reviews
              |   `-- reviews/ ReviewsOwnerFragment
`-- utils/ Extensions.kt, PreferencesManager, NotificationHelper

---

## Configuracion inicial

1. Abre Android Studio -> File -> Open -> selecciona la carpeta del proyecto
2. Espera que Gradle sincronice
3. Agrega tu Google Maps API Key en local.properties:
   MAPS_API_KEY=TU_CLAVE_AQUI
4. Coloca el archivo google-services.json (Firebase) en la carpeta app/
   Descargalo desde: Firebase Console -> tu proyecto -> Configuracion -> Android app
5. Ejecuta en emulador o dispositivo fisico (minSdk 24 = Android 7.0+)

---

## Funcionalidades

### Autenticacion

- Inicio de sesion con email y contrasena
- Registro con tipo de usuario: Conductor o Propietario
- Sesion persistente (no pide login cada vez que se abre la app)
- Recuperar contrasena por email (pantalla dedicada)
- Inicio de sesion con Google (pendiente de configurar SHA-1 en Firebase)

### Conductor

- Explorar estacionamientos con busqueda y mapa
- Ver detalle: precio, disponibilidad, ubicacion, resenas
- Reservar con pago integrado via Flow (pagos Chile)
- Historial de reservas con filtros y estadisticas
- Guardar estacionamientos favoritos
- Chat con propietarios
- Sistema de resenas

### Propietario

- Dashboard con estadisticas (ingresos, reservas, conductores)
- Gestionar estacionamientos (crear, editar, eliminar, activar/desactivar)
- Subir imagenes desde galeria o camara
- Ver y responder resenas
- Chat con conductores
- Graficos de estadisticas mensuales

### Notificaciones push (Firebase FCM)

- Reserva confirmada
- Pago rechazado
- Tiempo por vencer
- Recordatorio de resena
- Nueva reserva recibida (propietario)
- Pago recibido (propietario)
- Nueva resena (propietario)
- Estacionamiento lleno (propietario)
- Nuevo mensaje de chat (ambos)

---

## Tecnologias utilizadas

- Kotlin + MVVM + LiveData + Coroutines
- Jetpack Navigation Component + Safe Args
- Room Database (cache local)
- Retrofit + OkHttp (API REST)
- Firebase Cloud Messaging (notificaciones push)
- Google Maps SDK + Location Services
- Material Design 3
- DataStore Preferences (sesion persistente)
- Glide (carga de imagenes)
- MPAndroidChart (graficos de estadisticas)
- Flow (sistema de pagos Chile)

---

## Backend

URL base: http://161.153.192.177:3000/api/

El backend corre en una VM de Oracle Cloud con Node.js.

---

## Historial de versiones

| Version | Descripcion                                                                                                                     |
| ------- | ------------------------------------------------------------------------------------------------------------------------------- |
| 1.8.0   | Notificaciones push FCM, sesion persistente, pantalla recuperar contrasena, eliminar login Facebook, correcciones de navegacion |
| 1.0.0   | Version inicial: autenticacion, explorar, detalle, guardados, perfil                                                            |

---

## Pendiente para proximas versiones

- Completar inicio de sesion con Google (requiere SHA-1 en Firebase Console)
- Implementar endpoint forgot-password en el backend para envio real de emails
- Mejorar la integracion del mapa
- Boton de aceptar terminos y condiciones en registro

---

## Integrantes

Ver archivo Integrantes.txt
