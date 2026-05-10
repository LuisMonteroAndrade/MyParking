package com.miestacionamiento.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.miestacionamiento.R
import com.miestacionamiento.databinding.FragmentProfileBinding
import com.miestacionamiento.ui.auth.LoginActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userName.observe(viewLifecycleOwner) { binding.tvName.text = it }
        viewModel.userEmail.observe(viewLifecycleOwner) { binding.tvEmail.text = it }

        viewModel.isDarkMode.observe(viewLifecycleOwner) { enabled ->
            binding.switchDarkMode.isChecked = enabled
        }

        viewModel.language.observe(viewLifecycleOwner) { lang ->
            binding.chipEs.isChecked = lang == "es"
            binding.chipEn.isChecked = lang == "en"
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, checked ->
            viewModel.setDarkMode(checked)
            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.chipEs.setOnClickListener { viewModel.setLanguage("es") }
        binding.chipEn.setOnClickListener { viewModel.setLanguage("en") }

        binding.btnLogout.setOnClickListener { showLogoutDialog() }
        binding.btnEditProfile.setOnClickListener {
            com.google.android.material.snackbar.Snackbar
                .make(binding.root, "Función de edición próximamente", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
                .show()
        }
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
        super.onDestroyView()
        _binding = null
    }
}
