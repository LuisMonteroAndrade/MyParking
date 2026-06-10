package com.miestacionamiento.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.miestacionamiento.MiEstacionamientoApp
import com.miestacionamiento.data.local.entity.ParkingEntity
import com.miestacionamiento.databinding.FragmentExploreBinding
import kotlinx.coroutines.launch

class ExploreFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExploreViewModel by viewModels()
    private var googleMap: GoogleMap? = null

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

        viewModel.parkings.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvCount.text = "${list.size} estacionamientos encontrados"
            updateMapMarkers(list)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setQuery(newText.orEmpty())
                return true
            }
        })
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = false

        map.setOnInfoWindowClickListener { marker ->
            val parkingId = marker.tag as? Int ?: return@setOnInfoWindowClickListener
            findNavController().navigate(
                ExploreFragmentDirections.actionExploreToDetail(parkingId)
            )
        }

        viewModel.parkings.value?.let { updateMapMarkers(it) }
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
                    .snippet(parking.address)
            )
            marker?.tag = parking.id
            boundsBuilder.include(position)
        }

        try {
            val bounds = boundsBuilder.build()
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        } catch (e: Exception) {
            val first = parkings.first()
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(first.latitude, first.longitude), 14f))
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
