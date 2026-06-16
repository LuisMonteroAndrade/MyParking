package com.miestacionamiento.ui.explore

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.miestacionamiento.MiEstacionamientoApp
import com.miestacionamiento.data.local.entity.ParkingEntity
import com.miestacionamiento.databinding.FragmentExploreBinding
import kotlinx.coroutines.launch

class ExploreFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExploreViewModel by viewModels()
    private var googleMap: GoogleMap? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            enableMyLocation()
        } else {
            Snackbar.make(
                binding.root,
                "Activa los permisos de ubicación para ver tu posición en el mapa",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private val adapter = ParkingAdapter(
        onItemClick = { parking ->
            findNavController().navigate(
                ExploreFragmentDirections.actionExploreToDetail(parking.id)
            )
        },
        onSaveClick = { parking ->
            val repo = (requireActivity().application as MiEstacionamientoApp).repository
            viewModel.viewModelScope.launch {
                repo.toggleSaved(parking.id, !parking.isSaved)
            }
        }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        binding.rvParkings.adapter = adapter

        viewModel.refresh()

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val covered = (binding.mapView.height - bottomSheet.top).coerceAtLeast(0)
                googleMap?.setPadding(0, searchBarBottom(), 0, covered)
            }
        })

        viewModel.parkings.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvCount.text = "${list.size} disponibles"
            updateMapMarkers(list)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setQuery(newText.orEmpty())
                return true
            }
        })

        binding.fabMyLocation.setOnClickListener {
            if (hasLocationPermission()) {
                centerOnMyLocation()
            } else {
                requestLocationPermissions()
            }
        }
    }

    private fun searchBarBottom(): Int = try {
        binding.searchCard.bottom + (8 * resources.displayMetrics.density).toInt()
    } catch (e: Exception) { 0 }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = false

        binding.bottomSheet.post {
            val peekPx = bottomSheetBehavior.peekHeight
                .takeIf { it > 0 } ?: (180 * resources.displayMetrics.density).toInt()
            map.setPadding(0, searchBarBottom(), 0, peekPx)
        }

        map.setOnInfoWindowClickListener { marker ->
            val parkingId = marker.tag as? Int ?: return@setOnInfoWindowClickListener
            findNavController().navigate(
                ExploreFragmentDirections.actionExploreToDetail(parkingId)
            )
        }

        if (hasLocationPermission()) {
            enableMyLocation()
        } else {
            requestLocationPermissions()
        }

        viewModel.parkings.value?.let { updateMapMarkers(it) }
    }

    private fun hasLocationPermission(): Boolean =
        ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermissions() {
        locationPermissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        val map = googleMap ?: return
        map.isMyLocationEnabled = true
        centerOnMyLocation()
    }

    @SuppressLint("MissingPermission")
    private fun centerOnMyLocation() {
        if (!hasLocationPermission()) return
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (_binding == null) return@addOnSuccessListener
            if (location != null) {
                googleMap?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 14f)
                )
            }
        }
    }

    private fun updateMapMarkers(parkings: List<ParkingEntity>) {
        val map = googleMap ?: return
        map.clear()
        if (parkings.isEmpty()) return

        val boundsBuilder = LatLngBounds.Builder()
        parkings.forEach { parking ->
            val position = LatLng(parking.latitude, parking.longitude)
            val marker = map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(parking.name)
                    .snippet("${parking.address} · $${parking.pricePerHour.toLong()}/h")
            )
            marker?.tag = parking.id
            boundsBuilder.include(position)
        }

        try {
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120))
        } catch (e: Exception) {
            val first = parkings.first()
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(first.latitude, first.longitude), 12f))
        }
    }

    override fun onResume() { super.onResume(); binding.mapView.onResume() }
    override fun onPause() { super.onPause(); binding.mapView.onPause() }
    override fun onStart() { super.onStart(); binding.mapView.onStart() }
    override fun onStop() { super.onStop(); binding.mapView.onStop() }
    override fun onLowMemory() { super.onLowMemory(); binding.mapView.onLowMemory() }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _binding?.mapView?.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        binding.mapView.onDestroy()
        super.onDestroyView()
        _binding = null
    }
}
