package com.miestacionamiento.ui.auth

import android.content.Intent
import android.os.Bundle
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
        }
        binding.btnOwner.setOnClickListener {
            selectedUserType = "OWNER"
            binding.btnDriver.isSelected = false
            binding.btnOwner.isSelected = true
        }
        binding.btnDriver.isSelected = true
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
        binding.tvLogin.setOnClickListener { finish() }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }
}
