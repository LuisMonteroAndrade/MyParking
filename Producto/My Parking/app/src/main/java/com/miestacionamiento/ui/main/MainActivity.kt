package com.miestacionamiento.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.miestacionamiento.R
import com.miestacionamiento.data.remote.RetrofitClient
import com.miestacionamiento.databinding.ActivityMainBinding
import com.miestacionamiento.utils.NotificationHelper
import com.miestacionamiento.utils.PreferencesManager
import com.miestacionamiento.utils.gone
import com.miestacionamiento.utils.visible
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Resultado silencioso — el usuario decide */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        NotificationHelper.createChannels(this)
        requestNotificationPermissionIfNeeded()
        registerFcmToken()

        val userType = runBlocking {
            PreferencesManager(this@MainActivity).userType.first()
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val startDestId: Int
        val topLevelDestIds: Set<Int>

        if (userType == "OWNER") {
            startDestId = R.id.ownerDashboardFragment
            topLevelDestIds = setOf(
                R.id.ownerDashboardFragment,
                R.id.myParkingsFragment,
                R.id.chatListFragment,
                R.id.ownerStatsFragment,
                R.id.profileFragment
            )
            binding.bottomNav.menu.clear()
            menuInflater.inflate(R.menu.bottom_nav_owner_menu, binding.bottomNav.menu)
        } else {
            startDestId = R.id.homeFragment
            topLevelDestIds = setOf(
                R.id.homeFragment,
                R.id.exploreFragment,
                R.id.savedFragment,
                R.id.chatListFragment,
                R.id.profileFragment
            )
        }

        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        navGraph.setStartDestination(startDestId)
        navController.graph = navGraph

        binding.bottomNav.setupWithNavController(navController)

        handleNotificationIntent(intent, navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.detailFragment, R.id.parkingFormFragment,
                R.id.chatFragment, R.id.reviewsOwnerFragment,
                R.id.bookingHistoryFragment -> binding.bottomNav.gone()
                else -> binding.bottomNav.visible()
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentId = navController.currentDestination?.id
                if (currentId != startDestId && topLevelDestIds.contains(currentId)) {
                    binding.bottomNav.selectedItemId = startDestId
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment ?: return
        handleNotificationIntent(intent, navHostFragment.navController)
    }

    private fun registerFcmToken() {
        lifecycleScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                RetrofitClient.instance.registerFcmToken(mapOf("token" to token))
                Log.d("MainActivity", "FCM token registrado correctamente")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error registrando FCM token: ${e.message}")
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun handleNotificationIntent(intent: Intent?, navController: NavController) {
        val type = intent?.getStringExtra("notification_type") ?: return
        val extraId = intent.getIntExtra("notification_extra_id", 0)
        when (type) {
            "NEW_MESSAGE" -> {
                binding.bottomNav.selectedItemId = R.id.chatListFragment
                if (extraId != 0) {
                    navController.navigate(
                        R.id.action_chatList_to_chat,
                        bundleOf("conversationId" to extraId, "chatTitle" to "Chat")
                    )
                }
            }
            "NEW_BOOKING", "PAYMENT_RECEIVED", "PARKING_FULL" -> {
                binding.bottomNav.selectedItemId = R.id.ownerDashboardFragment
            }
            "NEW_REVIEW" -> {
                binding.bottomNav.selectedItemId = R.id.myParkingsFragment
            }
            "BOOKING_CONFIRMED", "BOOKING_EXPIRING", "REVIEW_REMINDER" -> {
                binding.bottomNav.selectedItemId = R.id.profileFragment
                navController.navigate(R.id.bookingHistoryFragment)
            }
            "BOOKING_FAILED" -> {
                binding.bottomNav.selectedItemId = R.id.homeFragment
            }
        }
    }
}
