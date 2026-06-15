package com.miestacionamiento.ui.auth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.miestacionamiento.databinding.ActivityLoginBinding
import com.miestacionamiento.ui.main.MainActivity
import com.miestacionamiento.utils.PreferencesManager
import com.miestacionamiento.utils.gone
import com.miestacionamiento.utils.visible
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var prefsManager: PreferencesManager

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* resultado ignorado: el usuario puede cambiar esto en ajustes */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = PreferencesManager(this)

        requestNotificationPermission()
        setupObservers()
        setupClicks()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun setupObservers() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.visible()
                    binding.btnLogin.isEnabled = false
                    binding.btnGoogle.isEnabled = false
                    binding.btnFacebook.isEnabled = false
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
                        viewModel.registerFcmToken()
                        navigateToMain()
                    }
                }
                is AuthState.Success -> {
                    binding.progressBar.gone()
                    navigateToMain()
                }
                is AuthState.Error -> {
                    binding.progressBar.gone()
                    binding.btnLogin.isEnabled = true
                    binding.btnGoogle.isEnabled = true
                    binding.btnFacebook.isEnabled = true
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupClicks() {
        binding.btnLogin.setOnClickListener {
            viewModel.login(
                binding.etEmail.text.toString().trim(),
                binding.etPassword.text.toString().trim()
            )
        }
        binding.btnGoogle.setOnClickListener { viewModel.loginWithGoogle() }
        binding.btnFacebook.setOnClickListener { viewModel.loginWithFacebook() }
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.tvForgotPassword.setOnClickListener {
            Snackbar.make(binding.root, "Se enviará un email para restablecer tu contraseña", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
