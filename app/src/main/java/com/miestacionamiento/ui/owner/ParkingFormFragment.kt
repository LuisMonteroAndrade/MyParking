package com.miestacionamiento.ui.owner

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.miestacionamiento.R
import com.miestacionamiento.data.model.OwnerParking
import com.miestacionamiento.databinding.FragmentParkingFormBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ParkingFormFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentParkingFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ParkingFormViewModel by viewModels()
    private val args: ParkingFormFragmentArgs by navArgs()

    private val isEditMode get() = args.parkingId != -1

    private var formMap: GoogleMap? = null
    private var mapViewBundle: Bundle? = null
    private var selectedLat = 0.0
    private var selectedLng = 0.0

    companion object {
        private const val MAP_VIEW_BUNDLE_KEY = "FormMapBundle"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentParkingFormBinding.inflate(inflater, container, false)
        mapViewBundle = savedInstanceState?.getBundle(MAP_VIEW_BUNDLE_KEY)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRegionDropdown()
        setupMapView()

        if (isEditMode) {
            binding.tvFormTitle.setText(R.string.form_edit_parking_title)
            binding.btnSubmit.setText(R.string.btn_update)
            viewModel.loadParking(args.parkingId)
        }

        observeViewModel()
        binding.btnSubmit.setOnClickListener { submitForm() }
        binding.btnLocate.setOnClickListener { geocodeAddress() }
    }

    private fun setupRegionDropdown() {
        val regionesComunas = linkedMapOf(
            "Arica y Parinacota" to listOf("Arica", "Camarones", "Putre", "General Lagos"),
            "Tarapacá" to listOf("Iquique", "Alto Hospicio", "Pozo Almonte", "Camiña", "Colchane", "Huara", "Pica"),
            "Antofagasta" to listOf("Antofagasta", "Mejillones", "Sierra Gorda", "Taltal", "Calama", "Ollagüe", "San Pedro de Atacama", "Tocopilla", "María Elena"),
            "Atacama" to listOf("Copiapó", "Caldera", "Tierra Amarilla", "Chañaral", "Diego de Almagro", "Vallenar", "Alto del Carmen", "Freirina", "Huasco"),
            "Coquimbo" to listOf("La Serena", "Coquimbo", "Andacollo", "La Higuera", "Paiguano", "Vicuña", "Illapel", "Canela", "Los Vilos", "Salamanca", "Ovalle", "Combarbalá", "Monte Patria", "Punitaqui", "Río Hurtado"),
            "Valparaíso" to listOf("Valparaíso", "Casablanca", "Concón", "Juan Fernández", "Puchuncaví", "Quintero", "Viña del Mar", "Isla de Pascua", "Los Andes", "Calle Larga", "Rinconada", "San Esteban", "La Ligua", "Cabildo", "Papudo", "Petorca", "Zapallar", "Quillota", "Calera", "Hijuelas", "La Cruz", "Nogales", "San Antonio", "Algarrobo", "Cartagena", "El Quisco", "El Tabo", "Santo Domingo", "San Felipe", "Catemu", "Llaillay", "Panquehue", "Putaendo", "Santa María", "Quilpué", "Limache", "Olmué", "Villa Alemana"),
            "Metropolitana de Santiago" to listOf("Santiago", "Cerrillos", "Cerro Navia", "Conchalí", "El Bosque", "Estación Central", "Huechuraba", "Independencia", "La Cisterna", "La Florida", "La Granja", "La Pintana", "La Reina", "Las Condes", "Lo Barnechea", "Lo Espejo", "Lo Prado", "Macul", "Maipú", "Ñuñoa", "Pedro Aguirre Cerda", "Peñalolén", "Providencia", "Pudahuel", "Quilicura", "Quinta Normal", "Recoleta", "Renca", "San Joaquín", "San Miguel", "San Ramón", "Vitacura", "Puente Alto", "Pirque", "San José de Maipo", "Colina", "Lampa", "Tiltil", "San Bernardo", "Buin", "Calera de Tango", "Paine", "Melipilla", "Alhué", "Curacaví", "María Pinto", "San Pedro", "Talagante", "El Monte", "Isla de Maipo", "Padre Hurtado", "Peñaflor"),
            "O'Higgins" to listOf("Rancagua", "Codegua", "Coinco", "Coltauco", "Doñihue", "Graneros", "Las Cabras", "Machalí", "Malloa", "Mostazal", "Olivar", "Peumo", "Pichidegua", "Quinta de Tilcoco", "Rengo", "Requínoa", "San Vicente", "Pichilemu", "La Estrella", "Litueche", "Marchihue", "Navidad", "Paredones", "San Fernando", "Chépica", "Chimbarongo", "Lolol", "Nancagua", "Palmilla", "Peralillo", "Placilla", "Pumanque", "Santa Cruz"),
            "Maule" to listOf("Talca", "Constitución", "Curepto", "Empedrado", "Maule", "Pelarco", "Pencahue", "Río Claro", "San Clemente", "San Rafael", "Cauquenes", "Chanco", "Pelluhue", "Curicó", "Hualañé", "Licantén", "Molina", "Rauco", "Romeral", "Sagrada Familia", "Teno", "Vichuquén", "Linares", "Colbún", "Longaví", "Parral", "Retiro", "San Javier", "Villa Alegre", "Yerbas Buenas"),
            "Ñuble" to listOf("Chillán", "Bulnes", "Chillán Viejo", "El Carmen", "Pemuco", "Pinto", "Quillón", "San Ignacio", "Yungay", "Quirihue", "Cobquecura", "Coelemu", "Ninhue", "Portezuelo", "Ránquil", "Treguaco", "San Carlos", "Coihueco", "Ñiquén", "San Fabián", "San Nicolás"),
            "Biobío" to listOf("Concepción", "Coronel", "Chiguayante", "Florida", "Hualpén", "Hualqui", "Lota", "Penco", "San Pedro de la Paz", "Santa Juana", "Talcahuano", "Tomé", "Los Ángeles", "Antuco", "Cabrero", "Laja", "Mulchén", "Nacimiento", "Negrete", "Quilaco", "Quilleco", "San Rosendo", "Santa Bárbara", "Tucapel", "Yumbel", "Arauco", "Cañete", "Contulmo", "Curanilahue", "Lebu", "Los Álamos", "Tirúa"),
            "La Araucanía" to listOf("Temuco", "Carahue", "Cunco", "Curarrehue", "Freire", "Galvarino", "Gorbea", "Lautaro", "Loncoche", "Melipeuco", "Nueva Imperial", "Padre Las Casas", "Perquenco", "Pitrufquén", "Pucón", "Saavedra", "Teodoro Schmidt", "Toltén", "Vilcún", "Villarrica", "Cholchol", "Angol", "Collipulli", "Curacautín", "Ercilla", "Lonquimay", "Los Sauces", "Lumaco", "Purén", "Renaico", "Traiguén", "Victoria"),
            "Los Ríos" to listOf("Valdivia", "Corral", "Futrono", "La Unión", "Lago Ranco", "Lanco", "Los Lagos", "Máfil", "Mariquina", "Paillaco", "Panguipulli", "Río Bueno"),
            "Los Lagos" to listOf("Puerto Montt", "Calbuco", "Cochamó", "Fresia", "Frutillar", "Los Muermos", "Llanquihue", "Maullín", "Puerto Varas", "Castro", "Ancud", "Chonchi", "Curaco de Vélez", "Dalcahue", "Puqueldón", "Queilén", "Quellón", "Quemchi", "Quinchao", "Osorno", "Puerto Octay", "Purranque", "Puyehue", "Río Negro", "San Juan de la Costa", "San Pablo", "Chaitén", "Futaleufú", "Hualaihué", "Palena"),
            "Aysén" to listOf("Coyhaique", "Lago Verde", "Aysén", "Cisnes", "Guaitecas", "Cochrane", "O'Higgins", "Tortel", "Chile Chico", "Río Ibáñez"),
            "Magallanes y la Antártica Chilena" to listOf("Punta Arenas", "Laguna Blanca", "Río Verde", "San Gregorio", "Cabo de Hornos", "Antártica", "Porvenir", "Primavera", "Timaukel", "Natales", "Torres del Paine")
        )

        val regiones = regionesComunas.keys.toList()
        val adapterRegiones = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, regiones)
        binding.actvRegion.setAdapter(adapterRegiones)

        binding.actvRegion.setOnItemClickListener { _, _, position, _ ->
            val regionSeleccionada = regiones[position]
            val comunas = regionesComunas[regionSeleccionada] ?: emptyList()
            val adapterComunas = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, comunas)
            binding.actvCommune.setText("")
            binding.actvCommune.setAdapter(adapterComunas)
        }
    }

    private fun setupMapView() {
        binding.mapViewForm.onCreate(mapViewBundle)
        binding.mapViewForm.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        formMap = map
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isScrollGesturesEnabled = true
        map.setOnMapClickListener { latLng ->
            selectedLat = latLng.latitude
            selectedLng = latLng.longitude
            updateMapMarker(latLng)
            showMap()
        }
        if (selectedLat != 0.0 && selectedLng != 0.0) {
            updateMapMarker(LatLng(selectedLat, selectedLng))
            showMap()
        }
    }

    private fun geocodeAddress() {
        val address = binding.etAddress.text?.toString()?.trim() ?: ""
        val commune = binding.actvCommune.text?.toString()?.trim() ?: ""
        val region = binding.actvRegion.text?.toString()?.trim() ?: ""

        if (address.isEmpty()) {
            binding.tilAddress.error = getString(R.string.error_required_fields)
            return
        }

        val fullAddress = buildString {
            append(address)
            if (commune.isNotEmpty()) append(", $commune")
            if (region.isNotEmpty()) append(", $region")
            append(", Chile")
        }

        binding.btnLocate.isEnabled = false
        binding.btnLocate.text = "Buscando..."

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    @Suppress("DEPRECATION")
                    val geocoder = Geocoder(requireContext(), Locale("es", "CL"))
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocationName(fullAddress, 1)?.firstOrNull()?.let { addr ->
                        LatLng(addr.latitude, addr.longitude)
                    }
                }.getOrNull()
            }

            if (_binding == null) return@launch

            binding.btnLocate.isEnabled = true
            binding.btnLocate.text = getString(R.string.btn_locate_map)

            if (result != null) {
                selectedLat = result.latitude
                selectedLng = result.longitude
                updateMapMarker(result)
                showMap()
            } else {
                Snackbar.make(binding.root, getString(R.string.error_location_not_found), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun updateMapMarker(position: LatLng) {
        formMap?.apply {
            clear()
            addMarker(MarkerOptions().position(position).title(binding.etName.text?.toString() ?: ""))
            moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
        }
    }

    private fun showMap() {
        binding.mapViewForm.visibility = View.VISIBLE
        binding.tvMapHelper.visibility = View.GONE
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnSubmit.isEnabled = !loading
        }

        viewModel.isSaving.observe(viewLifecycleOwner) { saving ->
            binding.progressBar.visibility = if (saving) View.VISIBLE else View.GONE
            binding.btnSubmit.isEnabled = !saving
        }

        viewModel.parking.observe(viewLifecycleOwner) { parking ->
            parking?.let { prefillForm(it) }
        }

        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            success?.let {
                val msg = if (isEditMode) getString(R.string.parking_updated_success)
                          else getString(R.string.parking_created_success)
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
                viewModel.clearMessages()
                findNavController().popBackStack()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearMessages()
            }
        }
    }

    private fun prefillForm(parking: OwnerParking) {
        binding.etName.setText(parking.name)
        binding.etDescription.setText(parking.description)
        binding.etAddress.setText(parking.address)
        binding.etPrice.setText(parking.pricePerHour.toInt().toString())
        binding.etAvailableSpots.setText(parking.availableSpots.toString())
        binding.etTotalSpots.setText(parking.totalSpots.toString())
        binding.etImageUrl.setText(parking.imageUrl)
        if (parking.latitude != 0.0 && parking.longitude != 0.0) {
            selectedLat = parking.latitude
            selectedLng = parking.longitude
            updateMapMarker(LatLng(parking.latitude, parking.longitude))
            showMap()
        }
    }

    private fun submitForm() {
        clearErrors()

        val name = binding.etName.text?.toString()?.trim() ?: ""
        val description = binding.etDescription.text?.toString()?.trim() ?: ""
        val address = binding.etAddress.text?.toString()?.trim() ?: ""
        val commune = binding.actvCommune.text?.toString()?.trim() ?: ""
        val region = binding.actvRegion.text?.toString()?.trim() ?: ""
        val priceStr = binding.etPrice.text?.toString()?.trim() ?: ""
        val availableSpotsStr = binding.etAvailableSpots.text?.toString()?.trim() ?: ""
        val totalSpotsStr = binding.etTotalSpots.text?.toString()?.trim() ?: ""
        val imageUrl = binding.etImageUrl.text?.toString()?.trim() ?: ""

        var valid = true

        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.error_required_fields)
            valid = false
        }
        if (address.isEmpty()) {
            binding.tilAddress.error = getString(R.string.error_required_fields)
            valid = false
        }
        val price = priceStr.toDoubleOrNull()
        if (priceStr.isEmpty() || price == null || price <= 0) {
            binding.tilPrice.error = getString(R.string.error_invalid_price)
            valid = false
        }

        if (!valid) return

        val availableSpots = availableSpotsStr.toIntOrNull() ?: 0
        val totalSpots = totalSpotsStr.toIntOrNull() ?: 0

        viewModel.saveParking(
            parkingId = args.parkingId,
            name = name,
            description = description,
            address = address,
            commune = commune,
            region = region,
            pricePerHour = price!!,
            availableSpots = availableSpots,
            totalSpots = totalSpots,
            imageUrl = imageUrl,
            latitude = selectedLat,
            longitude = selectedLng
        )
    }

    private fun clearErrors() {
        binding.tilName.error = null
        binding.tilAddress.error = null
        binding.tilPrice.error = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val mapBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY) ?: Bundle().also {
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, it)
        }
        _binding?.mapViewForm?.onSaveInstanceState(mapBundle)
    }

    override fun onResume() {
        super.onResume()
        binding.mapViewForm.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapViewForm.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapViewForm.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapViewForm.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapViewForm.onLowMemory()
    }

    override fun onDestroyView() {
        _binding?.mapViewForm?.onDestroy()
        super.onDestroyView()
        _binding = null
    }
}
