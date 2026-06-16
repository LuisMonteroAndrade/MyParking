Proyecto "Mi Estacionamiento"

  MiEstacionamiento/
  ├── settings.gradle / build.gradle / gradle.properties
  ├── local.properties          ← Aquí pones tu API Key de Google Maps
  └── app/
      ├── build.gradle
      └── src/main/
          ├── AndroidManifest.xml
          ├── java/com/miestacionamiento/
          │   ├── MiEstacionamientoApp.kt       ← Application class con Room
          │   ├── data/
          │   │   ├── model/       Parking.kt, User.kt
          │   │   ├── local/       AppDatabase, ParkingDao, ParkingEntity
          │   │   ├── remote/      ApiService (Retrofit), RetrofitClient
          │   │   └── repository/  ParkingRepository (con 8 parkings mock)
          │   ├── ui/
          │   │   ├── auth/        LoginActivity + VM, RegisterActivity + VM
          │   │   ├── main/        MainActivity (BottomNav + NavController)
          │   │   ├── home/        HomeFragment + VM + Adapter horizontal
          │   │   ├── explore/     ExploreFragment + VM + ParkingAdapter
          │   │   ├── detail/      DetailFragment + VM (Google Maps integrado)
          │   │   ├── saved/       SavedFragment + VM + grilla adaptador
          │   │   └── profile/     ProfileFragment + VM (idioma + dark mode)
          │   └── utils/           Extensions.kt, PreferencesManager (DataStore)
          └── res/
              ├── layout/          11 layouts (actividades + fragments + items)
              ├── navigation/      nav_graph.xml con Safe Args
              ├── menu/            bottom_nav_menu.xml
              ├── drawable/        22 íconos vectoriales + backgrounds
              ├── anim/            4 animaciones slide
              └── values/          strings, colors, themes (light+dark), dimens

  Pasos para abrir en Android Studio

  1. Abre Android Studio → File → Open → selecciona la carpeta MiEstacionamiento
  2. Espera que Gradle sincronice (descargará ~400MB de dependencias la primera vez)
  3. Agrega tu Google Maps API Key en local.properties:
  MAPS_API_KEY=TU_CLAVE_REAL_AQUI
  4. Obtén la clave gratis en https://console.cloud.google.com → APIs → Maps SDK for Android
  5. Run en un emulador o dispositivo físico (minSdk 24 = Android 7.0+)

  Funcionalidades incluidas

  ┌───────────┬────────────────────────────────────────────────────────────────────────────┐
  │ Pantalla  │                                  Función                                   │
  ├───────────┼────────────────────────────────────────────────────────────────────────────┤
  │ Login     │ Email/contraseña + botones Google y Facebook + validación                  │
  ├───────────┼────────────────────────────────────────────────────────────────────────────┤
  │ Registro  │ Toggle Conductor/Propietario + validación de campos                        │
  ├───────────┼────────────────────────────────────────────────────────────────────────────┤
  │ Home      │ Búsqueda, 8 parkings populares en cards horizontales, vistos recientemente │
  ├───────────┼────────────────────────────────────────────────────────────────────────────┤
  │ Explorar  │ SearchView en tiempo real, lista vertical con guardar/desguardar           │
  ├───────────┼────────────────────────────────────────────────────────────────────────────┤
  │ Detalle   │ Google Maps, barra de disponibilidad, diálogo de reserva                   │
  ├───────────┼────────────────────────────────────────────────────────────────────────────┤
  │ Guardados │ Grilla 2 columnas con Room database, estado vacío                          │
  ├───────────┼────────────────────────────────────────────────────────────────────────────┤
  │ Perfil    │ Modo oscuro (DataStore), selector de idioma ES/EN, cerrar sesión           │
  └───────────┴────────────────────────────────────────────────────────────────────────────┘

------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  ┌────────────────────────────────────────────────────────────────────────────────────────┐
  │                            NOTAS DE LOS DESARROLLADORES                                │
  └────────────────────────────────────────────────────────────────────────────────────────┘
  
Añadir para las siguiente version futuras del proyecto:
  - añadir funcionalidad del boton ""Editar Perfil"".
  - Añadir la funcion de pago con billetera google. (API billetera Google)
  - En la creacion de cuenta en el apartado de tipo usuario -> conductor: añadir los campos de añadir vehiculo, patente del vehiculo y una imagen.
  - En la creacion de cuenta en el apartado de tipo usuario -> propietatio: añadir los campos de añadir direccion, recion, comuna y una imagen del estacionamiento.
  - Añadir boton de aceptar terminos y condiciones en la pantalla de registro.
  - Añadir pantalla de propietario -> añadir estacinamiento.



Correcion de bugs o funcionalidades para la siguiente version:
  - Arreglar el boton de cambio de tema (El modo oscuro).
  - Arreglar el Mapa.
  - Correcion del mapa ya que no funciona




