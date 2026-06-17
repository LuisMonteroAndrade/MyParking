package com.miestacionamiento.ui.auth

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.miestacionamiento.databinding.ActivityForgotPasswordBinding
import com.miestacionamiento.utils.gone
import com.miestacionamiento.utils.visible

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupObservers()
        setupClicks()
    }

    private fun setupObservers() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is ForgotPasswordViewModel.State.Loading -> {
                    binding.progressBar.visible()
                    binding.btnSend.isEnabled = false
                }
                is ForgotPasswordViewModel.State.Sent -> {
                    binding.progressBar.gone()
                    binding.layoutForm.gone()
                    binding.layoutSuccess.visible()
                    binding.tvEmailSent.text =
                        "Enviamos instrucciones a ${state.email}"
                }
                is ForgotPasswordViewModel.State.Error -> {
                    binding.progressBar.gone()
                    binding.btnSend.isEnabled = true
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
                is ForgotPasswordViewModel.State.Idle -> Unit
            }
        }
    }

    private fun setupClicks() {
        binding.btnSend.setOnClickListener {
            viewModel.sendReset(binding.etEmail.text.toString())
        }
        binding.tvBackToLogin.setOnClickListener {
            finish()
        }
    }
}
