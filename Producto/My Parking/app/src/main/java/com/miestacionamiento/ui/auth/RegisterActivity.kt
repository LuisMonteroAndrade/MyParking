package com.miestacionamiento.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.miestacionamiento.databinding.ActivityRegisterBinding
import com.miestacionamiento.ui.main.MainActivity
import com.miestacionamiento.utils.PreferencesManager
import com.miestacionamiento.utils.gone
import com.miestacionamiento.utils.visible
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()
    private lateinit var prefsManager: PreferencesManager
    private var selectedUserType = "DRIVER"

    private var driverPhotoUri: Uri? = null
    private var ownerPhotoUri: Uri? = null
    private var currentPhotoTarget = ""

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            if (currentPhotoTarget == "driver") {
                driverPhotoUri = it
                binding.ivDriverPhoto.setImageURI(it)
                binding.ivDriverPhoto.visibility = View.VISIBLE
            } else {
                ownerPhotoUri = it
                binding.ivOwnerPhoto.setImageURI(it)
                binding.ivOwnerPhoto.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = PreferencesManager(this)

        setupUserTypeToggle()
        setupDropdownMarcas()
        setupDropdownRegionesYComunas()
        setupObservers()
        setupClicks()
    }

    private fun setupDropdownMarcas() {
        val marcasVehiculos = arrayOf(
            "Abarth", "Alfa Romeo", "Aston Martin", "Audi", "Austin", "BMW", "BYD", "Baic", "Bentley",
            "Bestune", "Brilliance", "Cadillac", "Changan", "Changhe", "Chery", "Chevrolet", "Chrysler",
            "Citroën", "Cupra", "DFSK", "DS Automobiles", "Dacia", "Daewoo", "Daihatsu", "Dodge",
            "Dongfeng", "Exeed", "FAW", "Ferrari", "Fiat", "Ford", "Foton", "GAC Motor", "GMC",
            "Geely", "Great Wall", "Haval", "Honda", "Hummer", "Hyundai", "Infiniti", "Isuzu", "Iveco",
            "JAC", "JMC", "Jaguar", "Jeep", "Jetour", "Kaiyi", "Karma", "Kia", "KyC", "Lamborghini",
            "Lancia", "Land Rover", "Lexus", "Lifan", "Lincoln", "Lotus", "MG", "Mahindra", "Maserati",
            "Maxus", "Mazda", "McLaren", "Mercedes-Benz", "Mini", "Mitsubishi", "Morgan", "Nissan",
            "Omoda", "Opel", "Peugeot", "Polestar", "Porsche", "Proton", "RAM", "Renault", "Rolls-Royce",
            "Rover", "Saab", "Samsung", "Seat", "Shineray", "Skoda", "Smart", "SsangYong", "Subaru",
            "Suzuki", "Tata", "Tesla", "Toyota", "Triumph", "Volkswagen", "Volvo", "Zotye", "Aprilia",
            "BMW Motorrad", "Bajaj", "Benelli", "CFMoto", "Can-Am", "Ducati", "Gas Gas", "Haojue",
            "Harley-Davidson", "Husqvarna", "Indian", "KTM", "Kawasaki", "Keeway", "Kymco", "MV Agusta",
            "Moto Guzzi", "Piaggio", "Royal Enfield", "SYM", "Vespa", "Yamaha", "Zontes"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, marcasVehiculos)
        binding.autoCompleteMarca.setAdapter(adapter)
    }

    private fun setupDropdownRegionesYComunas() {
        val regionesComunas = linkedMapOf(
            "Arica y Parinacota" to listOf(
                "Arica", "Camarones", "Putre", "General Lagos"
            ),
            "Tarapacá" to listOf(
                "Iquique", "Alto Hospicio", "Pozo Almonte", "Camiña", "Colchane", "Huara", "Pica"
            ),
            "Antofagasta" to listOf(
                "Antofagasta", "Mejillones", "Sierra Gorda", "Taltal", "Calama", "Ollagüe",
                "San Pedro de Atacama", "Tocopilla", "María Elena"
            ),
            "Atacama" to listOf(
                "Copiapó", "Caldera", "Tierra Amarilla", "Chañaral", "Diego de Almagro",
                "Vallenar", "Alto del Carmen", "Freirina", "Huasco"
            ),
            "Coquimbo" to listOf(
                "La Serena", "Coquimbo", "Andacollo", "La Higuera", "Paiguano", "Vicuña",
                "Illapel", "Canela", "Los Vilos", "Salamanca", "Ovalle", "Combarbalá",
                "Monte Patria", "Punitaqui", "Río Hurtado"
            ),
            "Valparaíso" to listOf(
                "Valparaíso", "Casablanca", "Concón", "Juan Fernández", "Puchuncaví", "Quintero",
                "Viña del Mar", "Isla de Pascua", "Los Andes", "Calle Larga", "Rinconada",
                "San Esteban", "La Ligua", "Cabildo", "Papudo", "Petorca", "Zapallar",
                "Quillota", "Calera", "Hijuelas", "La Cruz", "Nogales", "San Antonio",
                "Algarrobo", "Cartagena", "El Quisco", "El Tabo", "Santo Domingo", "San Felipe",
                "Catemu", "Llaillay", "Panquehue", "Putaendo", "Santa María", "Quilpué",
                "Limache", "Olmué", "Villa Alemana"
            ),
            "Metropolitana de Santiago" to listOf(
                "Santiago", "Cerrillos", "Cerro Navia", "Conchalí", "El Bosque", "Estación Central",
                "Huechuraba", "Independencia", "La Cisterna", "La Florida", "La Granja", "La Pintana",
                "La Reina", "Las Condes", "Lo Barnechea", "Lo Espejo", "Lo Prado", "Macul", "Maipú",
                "Ñuñoa", "Pedro Aguirre Cerda", "Peñalolén", "Providencia", "Pudahuel", "Quilicura",
                "Quinta Normal", "Recoleta", "Renca", "San Joaquín", "San Miguel", "San Ramón",
                "Vitacura", "Puente Alto", "Pirque", "San José de Maipo", "Colina", "Lampa", "Tiltil",
                "San Bernardo", "Buin", "Calera de Tango", "Paine", "Melipilla", "Alhué", "Curacaví",
                "María Pinto", "San Pedro", "Talagante", "El Monte", "Isla de Maipo", "Padre Hurtado",
                "Peñaflor"
            ),
            "O'Higgins" to listOf(
                "Rancagua", "Codegua", "Coinco", "Coltauco", "Doñihue", "Graneros", "Las Cabras",
                "Machalí", "Malloa", "Mostazal", "Olivar", "Peumo", "Pichidegua", "Quinta de Tilcoco",
                "Rengo", "Requínoa", "San Vicente", "Pichilemu", "La Estrella", "Litueche",
                "Marchihue", "Navidad", "Paredones", "San Fernando", "Chépica", "Chimbarongo",
                "Lolol", "Nancagua", "Palmilla", "Peralillo", "Placilla", "Pumanque", "Santa Cruz"
            ),
            "Maule" to listOf(
                "Talca", "Constitución", "Curepto", "Empedrado", "Maule", "Pelarco", "Pencahue",
                "Río Claro", "San Clemente", "San Rafael", "Cauquenes", "Chanco", "Pelluhue",
                "Curicó", "Hualañé", "Licantén", "Molina", "Rauco", "Romeral", "Sagrada Familia",
                "Teno", "Vichuquén", "Linares", "Colbún", "Longaví", "Parral", "Retiro",
                "San Javier", "Villa Alegre", "Yerbas Buenas"
            ),
            "Ñuble" to listOf(
                "Chillán", "Bulnes", "Chillán Viejo", "El Carmen", "Pemuco", "Pinto", "Quillón",
                "San Ignacio", "Yungay", "Quirihue", "Cobquecura", "Coelemu", "Ninhue",
                "Portezuelo", "Ránquil", "Treguaco", "San Carlos", "Coihueco", "Ñiquén",
                "San Fabián", "San Nicolás"
            ),
            "Biobío" to listOf(
                "Concepción", "Coronel", "Chiguayante", "Florida", "Hualpén", "Hualqui", "Lota",
                "Penco", "San Pedro de la Paz", "Santa Juana", "Talcahuano", "Tomé",
                "Los Ángeles", "Antuco", "Cabrero", "Laja", "Mulchén", "Nacimiento", "Negrete",
                "Quilaco", "Quilleco", "San Rosendo", "Santa Bárbara", "Tucapel", "Yumbel",
                "Arauco", "Cañete", "Contulmo", "Curanilahue", "Lebu", "Los Álamos", "Tirúa"
            ),
            "La Araucanía" to listOf(
                "Temuco", "Carahue", "Cunco", "Curarrehue", "Freire", "Galvarino", "Gorbea",
                "Lautaro", "Loncoche", "Melipeuco", "Nueva Imperial", "Padre Las Casas", "Perquenco",
                "Pitrufquén", "Pucón", "Saavedra", "Teodoro Schmidt", "Toltén", "Vilcún",
                "Villarrica", "Cholchol", "Angol", "Collipulli", "Curacautín", "Ercilla",
                "Lonquimay", "Los Sauces", "Lumaco", "Purén", "Renaico", "Traiguén", "Victoria"
            ),
            "Los Ríos" to listOf(
                "Valdivia", "Corral", "Futrono", "La Unión", "Lago Ranco", "Lanco", "Los Lagos",
                "Máfil", "Mariquina", "Paillaco", "Panguipulli", "Río Bueno"
            ),
            "Los Lagos" to listOf(
                "Puerto Montt", "Calbuco", "Cochamó", "Fresia", "Frutillar", "Los Muermos",
                "Llanquihue", "Maullín", "Puerto Varas", "Castro", "Ancud", "Chonchi",
                "Curaco de Vélez", "Dalcahue", "Puqueldón", "Queilén", "Quellón", "Quemchi",
                "Quinchao", "Osorno", "Puerto Octay", "Purranque", "Puyehue", "Río Negro",
                "San Juan de la Costa", "San Pablo", "Chaitén", "Futaleufú", "Hualaihué", "Palena"
            ),
            "Aysén" to listOf(
                "Coyhaique", "Lago Verde", "Aysén", "Cisnes", "Guaitecas", "Cochrane",
                "O'Higgins", "Tortel", "Chile Chico", "Río Ibáñez"
            ),
            "Magallanes y la Antártica Chilena" to listOf(
                "Punta Arenas", "Laguna Blanca", "Río Verde", "San Gregorio", "Cabo de Hornos",
                "Antártica", "Porvenir", "Primavera", "Timaukel", "Natales", "Torres del Paine"
            )
        )

        val regiones = regionesComunas.keys.toList()
        val adapterRegiones = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, regiones)
        binding.autoCompleteRegion.setAdapter(adapterRegiones)

        binding.autoCompleteRegion.setOnItemClickListener { _, _, position, _ ->
            val regionSeleccionada = regiones[position]
            val comunas = regionesComunas[regionSeleccionada] ?: emptyList()
            val adapterComunas = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, comunas)
            binding.autoCompleteCommune.setText("")
            binding.autoCompleteCommune.setAdapter(adapterComunas)
        }
    }

    private fun setupUserTypeToggle() {
        binding.btnDriver.setOnClickListener {
            selectedUserType = "DRIVER"
            binding.btnDriver.isSelected = true
            binding.btnOwner.isSelected = false
            binding.layoutDriverFields.visibility = View.VISIBLE
            binding.layoutOwnerFields.visibility = View.GONE
        }
        binding.btnOwner.setOnClickListener {
            selectedUserType = "OWNER"
            binding.btnDriver.isSelected = false
            binding.btnOwner.isSelected = true
            binding.layoutDriverFields.visibility = View.GONE
            binding.layoutOwnerFields.visibility = View.VISIBLE
        }
        binding.btnDriver.isSelected = true
        binding.layoutDriverFields.visibility = View.VISIBLE
        binding.layoutOwnerFields.visibility = View.GONE
    }

    private fun setupObservers() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.visible()
                    binding.btnRegister.isEnabled = false
                }
                is AuthState.SuccessWithData -> {
                    binding.progressBar.gone()
                    lifecycleScope.launch {
                        prefsManager.saveUserSession(
                            id = state.userId,
                            name = state.name,
                            email = state.email,
                            type = state.userType,
                            token = state.token,
                            vehicleBrand = state.vehicleBrand,
                            vehiclePlate = state.vehiclePlate,
                            address = state.address,
                            commune = state.commune,
                            region = state.region
                        )
                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                        finishAffinity()
                    }
                }
                is AuthState.Success -> {
                    binding.progressBar.gone()
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
                is AuthState.Error -> {
                    binding.progressBar.gone()
                    binding.btnRegister.isEnabled = true
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupClicks() {
        binding.btnRegister.setOnClickListener {
            val marcaSeleccionada = binding.autoCompleteMarca.text.toString().trim()
            val placa = binding.etLicensePlate.text.toString().trim()
            val direccion = binding.etAddress.text.toString().trim()
            val region = binding.autoCompleteRegion.text.toString().trim()
            val comuna = binding.autoCompleteCommune.text.toString().trim()

            if (selectedUserType == "DRIVER" && marcaSeleccionada.isEmpty()) {
                Snackbar.make(binding.root, "Por favor selecciona una marca de vehículo", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedUserType == "OWNER" && region.isEmpty()) {
                Snackbar.make(binding.root, "Por favor selecciona una región", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedUserType == "OWNER" && comuna.isEmpty()) {
                Snackbar.make(binding.root, "Por favor selecciona una comuna", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.register(
                name = binding.etName.text.toString().trim(),
                email = binding.etEmail.text.toString().trim(),
                password = binding.etPassword.text.toString().trim(),
                confirmPassword = binding.etConfirmPassword.text.toString().trim(),
                userType = selectedUserType,
                vehicleBrand = marcaSeleccionada.takeIf { it.isNotEmpty() },
                vehiclePlate = placa.takeIf { it.isNotEmpty() },
                address = direccion.takeIf { it.isNotEmpty() },
                commune = comuna.takeIf { it.isNotEmpty() },
                region = region.takeIf { it.isNotEmpty() }
            )
        }
        binding.btnSelectDriverPhoto.setOnClickListener {
            currentPhotoTarget = "driver"
            imagePickerLauncher.launch("image/*")
        }
        binding.btnSelectOwnerPhoto.setOnClickListener {
            currentPhotoTarget = "owner"
            imagePickerLauncher.launch("image/*")
        }
        binding.tvLogin.setOnClickListener { finish() }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }
}
