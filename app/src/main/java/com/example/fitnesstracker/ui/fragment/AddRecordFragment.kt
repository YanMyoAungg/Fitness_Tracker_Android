package com.example.fitnesstracker.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.model.ActivityType
import com.example.fitnesstracker.data.remote.SessionManager
import com.example.fitnesstracker.databinding.FragmentAddRecordBinding
import com.example.fitnesstracker.ui.viewmodel.FitnessViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.location.NominatimPOIProvider
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.*

class AddRecordFragment : Fragment() {

    private var _binding: FragmentAddRecordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FitnessViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLat: Double? = null
    private var currentLng: Double? = null
    private var currentLocationName: String? = "Unknown"
    private lateinit var mapView: MapView

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            fetchCurrentLocation()
        } else {
            Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupMap()
        setupSpinner()
        checkLocationPermission()

        binding.buttonSave.setOnClickListener { saveRecord() }
        observeViewModel()
    }

    private fun setupMap() {
        mapView = binding.mapView
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.controller.setZoom(15.0)
        mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
        mapView.setMultiTouchControls(true)
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fetchCurrentLocation()
            }

            else -> {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun fetchCurrentLocation() {
        try {
            val priority = Priority.PRIORITY_HIGH_ACCURACY
            val cts = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(priority, cts.token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        currentLat = location.latitude
                        currentLng = location.longitude
                        val userLocation = GeoPoint(location.latitude, location.longitude)
                        updateMapLocation(userLocation)
                        findNearbyFitnessCenters(userLocation) // Search for fitness centers
                        reverseGeocode(location.latitude, location.longitude)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to get location",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Location fetch error", Toast.LENGTH_SHORT)
                        .show()
                }
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Location permission error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMapLocation(location: GeoPoint) {
        mapView.controller.setCenter(location)
        val userMarker = Marker(mapView)
        userMarker.position = location
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        userMarker.title = "You are here"
        // Do not clear overlays here, so we can add more markers
        mapView.overlays.add(userMarker)
        mapView.invalidate()
    }

    private fun findNearbyFitnessCenters(location: GeoPoint) {
        lifecycleScope.launch(Dispatchers.IO) {
            val poiProvider = NominatimPOIProvider(Configuration.getInstance().userAgentValue)
            try {
                // Search for fitness centers, gyms, etc. within a 5km radius (50 results max)
                val pois = poiProvider.getPOICloseTo(location, "fitness", 50, 5.0)
                withContext(Dispatchers.Main) {
                    addPoisToMap(pois)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Error finding fitness centers: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun addPoisToMap(pois: List<POI>) {
        val fitnessIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_dumbbell_marker)
        pois.forEach { poi ->
            val poiMarker = Marker(mapView)
            poiMarker.title = poi.mType
            poiMarker.snippet = poi.mDescription
            poiMarker.position = poi.mLocation
            poiMarker.icon = fitnessIcon
            poiMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            mapView.overlays.add(poiMarker)
        }
        mapView.invalidate() // Redraw the map with the new markers
    }

    private fun reverseGeocode(lat: Double, lng: Double) {
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val city = address.locality ?: address.subAdminArea ?: ""
                val country = address.countryName ?: ""
                currentLocationName = if (city.isNotEmpty()) "$city, $country" else country
            }
        } catch (e: Exception) {
            currentLocationName = "Geocode error"
        }
    }

    private fun saveRecord() {
        val selectedDisplayName = binding.spinnerActivityType.selectedItem.toString()
        val selectedType = ActivityType.values().find { it.displayName == selectedDisplayName }
        val typeKey = selectedType?.databaseValue ?: selectedDisplayName

        val duration = binding.editTextDuration.text.toString().toIntOrNull() ?: 0
        val calories = binding.editTextCalories.text.toString().toIntOrNull() ?: 0

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDateTime = sdf.format(Date())

        val userId = sessionManager.getUserId()

        if (userId != -1) {
            if (duration > 0 && calories > 0) {
                viewModel.addRecord(
                    userId, typeKey, duration, calories, currentDateTime,
                    currentLat, currentLng, currentLocationName
                )
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter duration and calories",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.addRecordResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Record Saved", Toast.LENGTH_SHORT).show()
                binding.editTextDuration.text.clear()
                binding.editTextCalories.text.clear()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            ActivityType.values().map { it.displayName }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerActivityType.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        Configuration.getInstance()
            .load(requireContext(), requireContext().getSharedPreferences("osmdroid", 0))
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        Configuration.getInstance()
            .save(requireContext(), requireContext().getSharedPreferences("osmdroid", 0))
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
