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

        binding.chipEs.setOnClickListener { viewModel.setLanguage("es") }
        binding.chipEn.setOnClickListener { viewModel.setLanguage("en") }

        binding.cardPersonalInfo.setOnClickListener { showPersonalInfoDialog() }
        binding.btnEditProfile.setOnClickListener { showEditProfileDialog() }
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
        val tvInfoBrand = dialogView.findViewById<TextView>(R.id.tvInfoBrand)
        val tvInfoPlate = dialogView.findViewById<TextView>(R.id.tvInfoPlate)
        val ivVehiclePhoto = dialogView.findViewById<ImageView>(R.id.ivInfoVehiclePhoto)

        tvInfoName.text = currentName
        tvInfoEmail.text = currentEmail.ifEmpty { "Sin correo registrado" }
        tvInfoBrand.text = currentVehicleBrand.ifEmpty { "Sin información" }
        tvInfoPlate.text = currentLicensePlate.ifEmpty { "Sin información" }

        if (currentPhotoUri.isNotEmpty()) {
            Glide.with(this).load(Uri.parse(currentPhotoUri)).centerCrop()
                .placeholder(R.drawable.ic_person_large).into(ivAvatar)
        }
        if (currentVehiclePhotoUri.isNotEmpty()) {
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
        val tilVehicleBrand = dialogView.findViewById<TextInputLayout>(R.id.tilVehicleBrand)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etName)
        val autoCompleteBrand = dialogView.findViewById<AutoCompleteTextView>(R.id.autoCompleteVehicleBrand)
        val etLicensePlate = dialogView.findViewById<TextInputEditText>(R.id.etLicensePlate)
        val ivPhoto = dialogView.findViewById<ShapeableImageView>(R.id.ivProfilePhoto)
        val btnChangePhoto = dialogView.findViewById<ImageView>(R.id.btnChangePhoto)
        val ivVehiclePhoto = dialogView.findViewById<ImageView>(R.id.ivVehiclePhoto)
        val btnSelectVehiclePhoto = dialogView.findViewById<View>(R.id.btnSelectVehiclePhoto)

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

        etName.setText(currentName)
        if (currentVehicleBrand.isNotEmpty()) autoCompleteBrand.setText(currentVehicleBrand, false)
        etLicensePlate.setText(currentLicensePlate)

        selectedPhotoUri = if (currentPhotoUri.isNotEmpty()) Uri.parse(currentPhotoUri) else null
        selectedVehiclePhotoUri = if (currentVehiclePhotoUri.isNotEmpty()) Uri.parse(currentVehiclePhotoUri) else null

        dialogPhotoView = ivPhoto
        dialogVehiclePhotoView = ivVehiclePhoto

        if (currentPhotoUri.isNotEmpty()) {
            Glide.with(this).load(Uri.parse(currentPhotoUri)).centerCrop()
                .placeholder(R.drawable.ic_person_large).into(ivPhoto)
        }
        if (currentVehiclePhotoUri.isNotEmpty()) {
            ivVehiclePhoto.visibility = View.VISIBLE
            Glide.with(this).load(Uri.parse(currentVehiclePhotoUri)).centerCrop().into(ivVehiclePhoto)
        }

        btnChangePhoto.setOnClickListener { pickImageLauncher.launch("image/*") }
        btnSelectVehiclePhoto.setOnClickListener { pickVehicleImageLauncher.launch("image/*") }

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
                val brand = autoCompleteBrand.text?.toString()?.trim() ?: ""
                val plate = etLicensePlate.text?.toString()?.trim() ?: ""

                tilName.error = null
                tilVehicleBrand.error = null

                if (name.isEmpty()) {
                    tilName.error = getString(R.string.error_empty_name)
                    return@setOnClickListener
                }

                val photoUri = selectedPhotoUri?.toString() ?: currentPhotoUri
                val vehiclePhotoUri = selectedVehiclePhotoUri?.toString() ?: currentVehiclePhotoUri
                viewModel.saveProfile(name, currentEmail, photoUri)
                viewModel.saveVehicleInfo(brand, plate, vehiclePhotoUri)
                dialog.dismiss()
                Snackbar.make(binding.root, R.string.profile_updated, Snackbar.LENGTH_SHORT).show()
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
