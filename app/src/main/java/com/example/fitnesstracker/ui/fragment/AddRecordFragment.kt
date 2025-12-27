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
import com.example.fitnesstracker.data.model.ActivityType
import com.example.fitnesstracker.data.remote.SessionManager
import com.example.fitnesstracker.databinding.FragmentAddRecordBinding
import com.example.fitnesstracker.ui.viewmodel.FitnessViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            fetchCurrentLocation()
        } else {
            binding.textViewLocation.text = "Location: Permission denied"
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

        setupSpinner()
        checkLocationPermission()

        binding.buttonSave.setOnClickListener {
            saveRecord()
        }

        observeViewModel()
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
            // Using getCurrentLocation instead of getLastLocation for fresh coordinates
            val priority = Priority.PRIORITY_HIGH_ACCURACY
            val cts = CancellationTokenSource()
            
            fusedLocationClient.getCurrentLocation(priority, cts.token).addOnSuccessListener { location ->
                if (location != null) {
                    currentLat = location.latitude
                    currentLng = location.longitude
                    reverseGeocode(location.latitude, location.longitude)
                } else {
                    binding.textViewLocation.text = "Location: Unknown"
                }
            }.addOnFailureListener {
                binding.textViewLocation.text = "Location: Error fetching"
            }
        } catch (e: SecurityException) {
            binding.textViewLocation.text = "Location: Permission error"
        }
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
                binding.textViewLocation.text = "Location: $currentLocationName"
            } else {
                binding.textViewLocation.text = "Location: Unknown"
            }
        } catch (e: Exception) {
            binding.textViewLocation.text = "Location: Geocode error"
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
                Toast.makeText(requireContext(), "Please enter duration and calories", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.addRecordResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Saved at $currentLocationName", Toast.LENGTH_SHORT).show()
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
            android.R.layout.simple_spinner_item,
            ActivityType.values().map { it.displayName }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerActivityType.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
