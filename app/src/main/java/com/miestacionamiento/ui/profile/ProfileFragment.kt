package com.miestacionamiento.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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

    private var dialogPhotoView: ShapeableImageView? = null
    private var selectedPhotoUri: Uri? = null

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
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.personal_info)
            .setMessage(
                "${getString(R.string.hint_name)}: $currentName\n" +
                "${getString(R.string.hint_email)}: $currentEmail"
            )
            .setPositiveButton(R.string.edit_profile) { _, _ -> showEditProfileDialog() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val tilName = dialogView.findViewById<TextInputLayout>(R.id.tilName)
        val tilEmail = dialogView.findViewById<TextInputLayout>(R.id.tilEmail)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etName)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etEmail)
        val ivPhoto = dialogView.findViewById<ShapeableImageView>(R.id.ivProfilePhoto)
        val btnChangePhoto = dialogView.findViewById<ImageView>(R.id.btnChangePhoto)

        etName.setText(currentName)
        etEmail.setText(currentEmail)
        selectedPhotoUri = if (currentPhotoUri.isNotEmpty()) Uri.parse(currentPhotoUri) else null
        dialogPhotoView = ivPhoto

        if (currentPhotoUri.isNotEmpty()) {
            Glide.with(this)
                .load(Uri.parse(currentPhotoUri))
                .centerCrop()
                .placeholder(R.drawable.ic_person_large)
                .into(ivPhoto)
        }

        btnChangePhoto.setOnClickListener { pickImageLauncher.launch("image/*") }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_profile_title)
            .setView(dialogView)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.setOnDismissListener { dialogPhotoView = null }

        dialog.setOnShowListener {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = etName.text?.toString()?.trim() ?: ""
                val email = etEmail.text?.toString()?.trim() ?: ""

                tilName.error = null
                tilEmail.error = null

                var valid = true
                if (name.isEmpty()) {
                    tilName.error = getString(R.string.error_empty_name)
                    valid = false
                }
                if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    tilEmail.error = getString(R.string.error_invalid_email)
                    valid = false
                }

                if (valid) {
                    val photoUri = selectedPhotoUri?.toString() ?: currentPhotoUri
                    viewModel.saveProfile(name, email, photoUri)
                    dialog.dismiss()
                    Snackbar.make(binding.root, R.string.profile_updated, Snackbar.LENGTH_SHORT).show()
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
        super.onDestroyView()
        _binding = null
    }
}
