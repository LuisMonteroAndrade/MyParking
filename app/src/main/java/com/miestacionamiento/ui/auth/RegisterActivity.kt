package com.miestacionamiento.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.miestacionamiento.databinding.ActivityRegisterBinding
import com.miestacionamiento.ui.main.MainActivity
import com.miestacionamiento.utils.gone
import com.miestacionamiento.utils.visible

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()
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

        setupUserTypeToggle()
        setupObservers()
        setupClicks()
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
            viewModel.register(
                binding.etName.text.toString().trim(),
                binding.etEmail.text.toString().trim(),
                binding.etPassword.text.toString().trim(),
                binding.etConfirmPassword.text.toString().trim(),
                selectedUserType
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
