package com.miestacionamiento.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.miestacionamiento.R
import com.miestacionamiento.databinding.FragmentProfileBinding
import com.miestacionamiento.ui.auth.LoginActivity
import com.miestacionamiento.utils.gone
import com.miestacionamiento.utils.visible
import androidx.navigation.fragment.findNavController

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    private var currentName: String = ""
    private var currentEmail: String = ""
    private var currentPhotoUri: String = ""
    private var currentVehicleBrand: String = ""
    private var currentLicensePlate: String = ""
    private var currentVehiclePhotoUri: String = ""
    private var currentUserType: String = "DRIVER"
    private var currentIsDualRole: Boolean = false
    private var currentAddress: String = ""
    private var currentCommune: String = ""
    private var currentRegion: String = ""

    private var dialogPhotoView: ShapeableImageView? = null
    private var dialogVehiclePhotoView: ImageView? = null
    private var selectedPhotoUri: Uri? = null
    private var selectedVehiclePhotoUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) { }
            selectedPhotoUri = it
            dialogPhotoView?.let { iv ->
                Glide.with(this).load(it).centerCrop().into(iv)
            }
        }
    }

    private val pickVehicleImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) { }
            selectedVehiclePhotoUri = it
            dialogVehiclePhotoView?.let { iv ->
                iv.visibility = View.VISIBLE
                Glide.with(this).load(it).centerCrop().into(iv)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userName.observe(viewLifecycleOwner) {
            binding.tvName.text = it
            currentName = it
        }
        viewModel.userEmail.observe(viewLifecycleOwner) {
            binding.tvEmail.text = it
            currentEmail = it
        }
        viewModel.profileImageUri.observe(viewLifecycleOwner) { uriStr ->
            currentPhotoUri = uriStr
            if (uriStr.isNotEmpty()) {
                Glide.with(this)
                    .load(Uri.parse(uriStr))
                    .centerCrop()
                    .placeholder(R.drawable.ic_person_large)
                    .into(binding.ivProfileAvatar)
            } else {
                binding.ivProfileAvatar.setImageResource(R.drawable.ic_person_large)
            }
        }
        viewModel.vehicleBrand.observe(viewLifecycleOwner) { currentVehicleBrand = it }
        viewModel.licensePlate.observe(viewLifecycleOwner) { currentLicensePlate = it }
        viewModel.vehiclePhotoUri.observe(viewLifecycleOwner) { currentVehiclePhotoUri = it }
        viewModel.userType.observe(viewLifecycleOwner) { type ->
            currentUserType = type
            updateRoleButtons()
        }
        viewModel.isDualRole.observe(viewLifecycleOwner) { dual ->
            currentIsDualRole = dual
            updateRoleButtons()
        }
        viewModel.userAddress.observe(viewLifecycleOwner) { currentAddress = it }
        viewModel.userCommune.observe(viewLifecycleOwner) { currentCommune = it }
        viewModel.userRegion.observe(viewLifecycleOwner) { currentRegion = it }

        viewModel.isDarkMode.observe(viewLifecycleOwner) { enabled ->
            binding.switchDarkMode.setOnClickListener(null)
            binding.switchDarkMode.isChecked = enabled
            binding.switchDarkMode.setOnClickListener { onDarkModeClicked() }
        }

        viewModel.notificationsEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.switchNotifications.setOnClickListener(null)
            binding.switchNotifications.isChecked = enabled
            binding.tvNotificationsStatus.setText(
                if (enabled) R.string.notifications_enabled else R.string.notifications_disabled
            )
            binding.switchNotifications.setOnClickListener { onNotificationsClicked() }
        }

        viewModel.language.observe(viewLifecycleOwner) { lang ->
            binding.chipEs.isChecked = lang == "es"
            binding.chipEn.isChecked = lang == "en"
        }
        viewModel.changeRoleError.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.changeRoleError.value = null
            }
        }

        binding.chipEs.setOnClickListener { viewModel.setLanguage("es") }
        binding.chipEn.setOnClickListener { viewModel.setLanguage("en") }

        binding.cardPersonalInfo.setOnClickListener { showPersonalInfoDialog() }
        binding.cardBookingHistory.setOnClickListener {
            findNavController().navigate(
                com.miestacionamiento.ui.profile.ProfileFragmentDirections
                    .actionProfileToBookingHistory()
            )
        }
        binding.btnEditProfile.setOnClickListener { showEditProfileDialog() }
        binding.btnBecomeOwner.setOnClickListener { showBecomeOwnerDialog() }
        binding.btnSwitchToDriver.setOnClickListener { showSwitchToDriverDialog() }
        binding.btnLogout.setOnClickListener { showLogoutDialog() }
    }

    private fun onDarkModeClicked() {
        val checked = binding.switchDarkMode.isChecked
        viewModel.setDarkMode(checked)
        AppCompatDelegate.setDefaultNightMode(
            if (checked) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun onNotificationsClicked() {
        val checked = binding.switchNotifications.isChecked
        viewModel.setNotificationsEnabled(checked)
        binding.tvNotificationsStatus.setText(
            if (checked) R.string.notifications_enabled else R.string.notifications_disabled
        )
    }

    private fun showPersonalInfoDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_personal_info, null)
        val ivAvatar = dialogView.findViewById<ShapeableImageView>(R.id.ivInfoAvatar)
        val tvInfoName = dialogView.findViewById<TextView>(R.id.tvInfoName)
        val tvInfoEmail = dialogView.findViewById<TextView>(R.id.tvInfoEmail)
        val layoutVehicleSection = dialogView.findViewById<View>(R.id.layoutVehicleSection)
        val tvInfoBrand = dialogView.findViewById<TextView>(R.id.tvInfoBrand)
        val tvInfoPlate = dialogView.findViewById<TextView>(R.id.tvInfoPlate)
        val ivVehiclePhoto = dialogView.findViewById<ImageView>(R.id.ivInfoVehiclePhoto)
        val layoutOwnerSection = dialogView.findViewById<View>(R.id.layoutOwnerSection)
        val tvInfoAddress = dialogView.findViewById<TextView>(R.id.tvInfoAddress)
        val tvInfoCommune = dialogView.findViewById<TextView>(R.id.tvInfoCommune)
        val tvInfoRegion = dialogView.findViewById<TextView>(R.id.tvInfoRegion)

        tvInfoName.text = currentName
        tvInfoEmail.text = currentEmail.ifEmpty { "Sin correo registrado" }

        if (currentUserType == "OWNER") {
            layoutVehicleSection.visibility = View.GONE
            layoutOwnerSection.visibility = View.VISIBLE
            tvInfoAddress.text = currentAddress.ifEmpty { "Sin información" }
            tvInfoCommune.text = currentCommune.ifEmpty { "Sin información" }
            tvInfoRegion.text = currentRegion.ifEmpty { "Sin información" }
        } else {
            layoutVehicleSection.visibility = View.VISIBLE
            layoutOwnerSection.visibility = View.GONE
            tvInfoBrand.text = currentVehicleBrand.ifEmpty { "Sin información" }
            tvInfoPlate.text = currentLicensePlate.ifEmpty { "Sin información" }
        }

        if (currentPhotoUri.isNotEmpty()) {
            Glide.with(this).load(Uri.parse(currentPhotoUri)).centerCrop()
                .placeholder(R.drawable.ic_person_large).into(ivAvatar)
        }
        if (currentVehiclePhotoUri.isNotEmpty() && currentUserType != "OWNER") {
            ivVehiclePhoto.visibility = View.VISIBLE
            Glide.with(this).load(Uri.parse(currentVehiclePhotoUri)).centerCrop().into(ivVehiclePhoto)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setPositiveButton(R.string.edit_profile) { _, _ -> showEditProfileDialog() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val tilName = dialogView.findViewById<TextInputLayout>(R.id.tilName)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etName)
        val ivPhoto = dialogView.findViewById<ShapeableImageView>(R.id.ivProfilePhoto)
        val btnChangePhoto = dialogView.findViewById<ImageView>(R.id.btnChangePhoto)
        val layoutDriverSection = dialogView.findViewById<View>(R.id.layoutDriverSection)
        val layoutOwnerSection = dialogView.findViewById<View>(R.id.layoutOwnerSection)

        etName.setText(currentName)
        selectedPhotoUri = if (currentPhotoUri.isNotEmpty()) Uri.parse(currentPhotoUri) else null
        dialogPhotoView = ivPhoto

        if (currentPhotoUri.isNotEmpty()) {
            Glide.with(this).load(Uri.parse(currentPhotoUri)).centerCrop()
                .placeholder(R.drawable.ic_person_large).into(ivPhoto)
        }

        btnChangePhoto.setOnClickListener { pickImageLauncher.launch("image/*") }

        if (currentUserType == "OWNER") {
            layoutDriverSection.visibility = View.GONE
            layoutOwnerSection.visibility = View.VISIBLE
            setupOwnerEditSection(dialogView)
        } else {
            layoutDriverSection.visibility = View.VISIBLE
            layoutOwnerSection.visibility = View.GONE
            setupDriverEditSection(dialogView)
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_profile_title)
            .setView(dialogView)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.setOnDismissListener {
            dialogPhotoView = null
            dialogVehiclePhotoView = null
        }

        dialog.setOnShowListener {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = etName.text?.toString()?.trim() ?: ""
                tilName.error = null
                if (name.isEmpty()) {
                    tilName.error = getString(R.string.error_empty_name)
                    return@setOnClickListener
                }
                val photoUri = selectedPhotoUri?.toString() ?: currentPhotoUri
                viewModel.saveProfile(name, currentEmail, photoUri)

                if (currentUserType == "OWNER") {
                    val etAddress = dialogView.findViewById<TextInputEditText>(R.id.etOwnerAddress)
                    val autoRegion = dialogView.findViewById<AutoCompleteTextView>(R.id.autoCompleteEditRegion)
                    val autoCommune = dialogView.findViewById<AutoCompleteTextView>(R.id.autoCompleteEditCommune)
                    val address = etAddress.text?.toString()?.trim() ?: ""
                    val region = autoRegion.text?.toString()?.trim() ?: ""
                    val commune = autoCommune.text?.toString()?.trim() ?: ""
                    viewModel.saveOwnerInfo(address, commune, region)
                } else {
                    val autoCompleteBrand = dialogView.findViewById<AutoCompleteTextView>(R.id.autoCompleteVehicleBrand)
                    val etLicensePlate = dialogView.findViewById<TextInputEditText>(R.id.etLicensePlate)
                    val brand = autoCompleteBrand.text?.toString()?.trim() ?: ""
                    val plate = etLicensePlate.text?.toString()?.trim() ?: ""
                    val vehiclePhotoUri = selectedVehiclePhotoUri?.toString() ?: currentVehiclePhotoUri
                    viewModel.saveVehicleInfo(brand, plate, vehiclePhotoUri)
                }

                dialog.dismiss()
                Snackbar.make(binding.root, R.string.profile_updated, Snackbar.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun setupDriverEditSection(dialogView: android.view.View) {
        val autoCompleteBrand = dialogView.findViewById<AutoCompleteTextView>(R.id.autoCompleteVehicleBrand)
        val etLicensePlate = dialogView.findViewById<TextInputEditText>(R.id.etLicensePlate)
        val ivVehiclePhoto = dialogView.findViewById<ImageView>(R.id.ivVehiclePhoto)
        val btnSelectVehiclePhoto = dialogView.findViewById<android.view.View>(R.id.btnSelectVehiclePhoto)

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
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, marcasVehiculos)
        autoCompleteBrand.setAdapter(adapter)

        if (currentVehicleBrand.isNotEmpty()) autoCompleteBrand.setText(currentVehicleBrand, false)
        etLicensePlate.setText(currentLicensePlate)
        selectedVehiclePhotoUri = if (currentVehiclePhotoUri.isNotEmpty()) Uri.parse(currentVehiclePhotoUri) else null
        dialogVehiclePhotoView = ivVehiclePhoto

        if (currentVehiclePhotoUri.isNotEmpty()) {
            ivVehiclePhoto.visibility = View.VISIBLE
            Glide.with(this).load(Uri.parse(currentVehiclePhotoUri)).centerCrop().into(ivVehiclePhoto)
        }
        btnSelectVehiclePhoto.setOnClickListener { pickVehicleImageLauncher.launch("image/*") }
    }

    private fun setupOwnerEditSection(dialogView: android.view.View) {
        val etAddress = dialogView.findViewById<TextInputEditText>(R.id.etOwnerAddress)
        val autoRegion = dialogView.findViewById<AutoCompleteTextView>(R.id.autoCompleteEditRegion)
        val autoCommune = dialogView.findViewById<AutoCompleteTextView>(R.id.autoCompleteEditCommune)

        val regionesComunas = linkedMapOf(
            "Arica y Parinacota" to listOf("Arica", "Camarones", "Putre", "General Lagos"),
            "Tarapacá" to listOf("Iquique", "Alto Hospicio", "Pozo Almonte", "Camiña", "Colchane", "Huara", "Pica"),
            "Antofagasta" to listOf("Antofagasta", "Mejillones", "Sierra Gorda", "Taltal", "Calama", "Ollagüe", "San Pedro de Atacama", "Tocopilla", "María Elena"),
            "Atacama" to listOf("Copiapó", "Caldera", "Tierra Amarilla", "Chañaral", "Diego de Almagro", "Vallenar", "Alto del Carmen", "Freirina", "Huasco"),
            "Coquimbo" to listOf("La Serena", "Coquimbo", "Andacollo", "La Higuera", "Paiguano", "Vicuña", "Illapel", "Canela", "Los Vilos", "Salamanca", "Ovalle", "Combarbalá", "Monte Patria", "Punitaqui", "Río Hurtado"),
            "Valparaíso" to listOf("Valparaíso", "Casablanca", "Concón", "Juan Fernández", "Puchuncaví", "Quintero", "Viña del Mar", "Isla de Pascua", "Los Andes", "Calle Larga", "Rinconada", "San Esteban", "La Ligua", "Cabildo", "Papudo", "Petorca", "Zapallar", "Quillota", "Calera", "Hijuelas", "La Cruz", "Nogales", "San Antonio", "Algarrobo", "Cartagena", "El Quisco", "El Tabo", "Santo Domingo", "San Felipe", "Catemu", "Llaillay", "Panquehue", "Putaendo", "Santa María", "Quilpué", "Limache", "Olmué", "Villa Alemana"),
            "Metropolitana de Santiago" to listOf("Santiago", "Cerrillos", "Cerro Navia", "Conchalí", "El Bosque", "Estación Central", "Huechuraba", "Independencia", "La Cisterna", "La Florida", "La Granja", "La Pintana", "La Reina", "Las Condes", "Lo Barnechea", "Lo Espejo", "Lo Prado", "Macul", "Maipú", "Ñuñoa", "Pedro Aguirre Cerda", "Peñalolén", "Providencia", "Pudahuel", "Quilicura", "Quinta Normal", "Recoleta", "Renca", "San Joaquín", "San Miguel", "San Ramón", "Vitacura", "Puente Alto", "Pirque", "San José de Maipo", "Colina", "Lampa", "Tiltil", "San Bernardo", "Buin", "Calera de Tango", "Paine", "Melipilla", "Alhué", "Curacaví", "María Pinto", "San Pedro", "Talagante", "El Monte", "Isla de Maipo", "Padre Hurtado", "Peñaflor"),
            "O'Higgins" to listOf("Rancagua", "Codegua", "Coinco", "Coltauco", "Doñihue", "Graneros", "Las Cabras", "Machalí", "Malloa", "Mostazal", "Olivar", "Peumo", "Pichidegua", "Quinta de Tilcoco", "Rengo", "Requínoa", "San Vicente", "Pichilemu", "La Estrella", "Litueche", "Marchihue", "Navidad", "Paredones", "San Fernando", "Chépica", "Chimbarongo", "Lolol", "Nancagua", "Palmilla", "Peralillo", "Placilla", "Pumanque", "Santa Cruz"),
            "Maule" to listOf("Talca", "Constitución", "Curepto", "Empedrado", "Maule", "Pelarco", "Pencahue", "Río Claro", "San Clemente", "San Rafael", "Cauquenes", "Chanco", "Pelluhue", "Curicó", "Hualañé", "Licantén", "Molina", "Rauco", "Romeral", "Sagrada Familia", "Teno", "Vichuquén", "Linares", "Colbún", "Longaví", "Parral", "Retiro", "San Javier", "Villa Alegre", "Yerbas Buenas"),
            "Ñuble" to listOf("Chillán", "Bulnes", "Chillán Viejo", "El Carmen", "Pemuco", "Pinto", "Quillón", "San Ignacio", "Yungay", "Quirihue", "Cobquecura", "Coelemu", "Ninhue", "Portezuelo", "Ránquil", "Treguaco", "San Carlos", "Coihueco", "Ñiquén", "San Fabián", "San Nicolás"),
            "Biobío" to listOf("Concepción", "Coronel", "Chiguayante", "Florida", "Hualpén", "Hualqui", "Lota", "Penco", "San Pedro de la Paz", "Santa Juana", "Talcahuano", "Tomé", "Los Ángeles", "Antuco", "Cabrero", "Laja", "Mulchén", "Nacimiento", "Negrete", "Quilaco", "Quilleco", "San Rosendo", "Santa Bárbara", "Tucapel", "Yumbel", "Arauco", "Cañete", "Contulmo", "Curanilahue", "Lebu", "Los Álamos", "Tirúa"),
            "La Araucanía" to listOf("Temuco", "Carahue", "Cunco", "Curarrehue", "Freire", "Galvarino", "Gorbea", "Lautaro", "Loncoche", "Melipeuco", "Nueva Imperial", "Padre Las Casas", "Perquenco", "Pitrufquén", "Pucón", "Saavedra", "Teodoro Schmidt", "Toltén", "Vilcún", "Villarrica", "Cholchol", "Angol", "Collipulli", "Curacautín", "Ercilla", "Lonquimay", "Los Sauces", "Lumaco", "Purén", "Renaico", "Traiguén", "Victoria"),
            "Los Ríos" to listOf("Valdivia", "Corral", "Futrono", "La Unión", "Lago Ranco", "Lanco", "Los Lagos", "Máfil", "Mariquina", "Paillaco", "Panguipulli", "Río Bueno"),
            "Los Lagos" to listOf("Puerto Montt", "Calbuco", "Cochamó", "Fresia", "Frutillar", "Los Muermos", "Llanquihue", "Maullín", "Puerto Varas", "Castro", "Ancud", "Chonchi", "Curaco de Vélez", "Dalcahue", "Puqueldón", "Queilén", "Quellón", "Quemchi", "Quinchao", "Osorno", "Puerto Octay", "Purranque", "Puyehue", "Río Negro", "San Juan de la Costa", "San Pablo", "Chaitén", "Futaleufú", "Hualaihué", "Palena"),
            "Aysén" to listOf("Coyhaique", "Lago Verde", "Aysén", "Cisnes", "Guaitecas", "Cochrane", "O'Higgins", "Tortel", "Chile Chico", "Río Ibáñez"),
            "Magallanes y la Antártica Chilena" to listOf("Punta Arenas", "Laguna Blanca", "Río Verde", "San Gregorio", "Cabo de Hornos", "Antártica", "Porvenir", "Primavera", "Timaukel", "Natales", "Torres del Paine")
        )
        val regiones = regionesComunas.keys.toList()
        val adapterRegiones = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, regiones)
        autoRegion.setAdapter(adapterRegiones)

        if (currentRegion.isNotEmpty()) {
            autoRegion.setText(currentRegion, false)
            val comunas = regionesComunas[currentRegion] ?: emptyList()
            val adapterComunas = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, comunas)
            autoCommune.setAdapter(adapterComunas)
            if (currentCommune.isNotEmpty()) autoCommune.setText(currentCommune, false)
        }

        autoRegion.setOnItemClickListener { _, _, position, _ ->
            val regionSel = regiones[position]
            val comunas = regionesComunas[regionSel] ?: emptyList()
            val adapterComunas = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, comunas)
            autoCommune.setText("")
            autoCommune.setAdapter(adapterComunas)
        }

        etAddress.setText(currentAddress)
    }

    private fun updateRoleButtons() {
        val isOwner = currentUserType == "OWNER"
        if (isOwner) binding.cardBookingHistory.gone() else binding.cardBookingHistory.visible()
        // "¿Quieres ser propietario?" solo aparece si es conductor y NO tiene doble rol
        if (!isOwner && !currentIsDualRole) binding.btnBecomeOwner.visible() else binding.btnBecomeOwner.gone()
        // "Volver a conductor" solo aparece si es propietario Y tiene doble rol
        if (isOwner && currentIsDualRole) binding.btnSwitchToDriver.visible() else binding.btnSwitchToDriver.gone()
    }

    private fun showSwitchToDriverDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Volver a cuenta de conductor")
            .setMessage("Cambiarás tu vista a la cuenta de conductor. Tus datos de propietario se conservarán y podrás volver cuando quieras.")
            .setPositiveButton("Confirmar") { _, _ ->
                viewModel.switchToDriver {
                    Snackbar.make(binding.root, "Cambiando a cuenta de conductor...", Snackbar.LENGTH_LONG).show()
                    binding.root.postDelayed({ requireActivity().recreate() }, 1000)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showBecomeOwnerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_become_owner, null)
        setupOwnerEditSection(dialogView)

        val tilAddress = dialogView.findViewById<TextInputLayout>(R.id.tilOwnerAddressBecome)
        val etAddress = dialogView.findViewById<TextInputEditText>(R.id.etOwnerAddress)
        val autoRegion = dialogView.findViewById<AutoCompleteTextView>(R.id.autoCompleteEditRegion)
        val autoCommune = dialogView.findViewById<AutoCompleteTextView>(R.id.autoCompleteEditCommune)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Convertirte en propietario")
            .setView(dialogView)
            .setPositiveButton("Confirmar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val address = etAddress.text?.toString()?.trim() ?: ""
                val region = autoRegion.text?.toString()?.trim() ?: ""
                val commune = autoCommune.text?.toString()?.trim() ?: ""

                tilAddress.error = null
                if (address.isEmpty()) {
                    tilAddress.error = "Ingresa una dirección"
                    return@setOnClickListener
                }
                if (region.isEmpty()) {
                    Snackbar.make(binding.root, "Selecciona una región", Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (commune.isEmpty()) {
                    Snackbar.make(binding.root, "Selecciona una comuna", Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                dialog.dismiss()
                viewModel.upgradeToOwner(address, commune, region) {
                    Snackbar.make(binding.root, "¡Ahora eres propietario! Reiniciando...", Snackbar.LENGTH_LONG).show()
                    binding.root.postDelayed({ requireActivity().recreate() }, 1500)
                }
            }
        }

        dialog.show()
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro que deseas cerrar sesión?")
            .setPositiveButton("Cerrar sesión") { _, _ ->
                viewModel.logout {
                    startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        dialogPhotoView = null
        dialogVehiclePhotoView = null
        super.onDestroyView()
        _binding = null
    }
}
