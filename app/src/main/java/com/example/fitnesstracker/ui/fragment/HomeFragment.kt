package com.example.fitnesstracker.ui.fragment

import android.Manifest
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.fitnesstracker.MainActivity
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.remote.SessionManager
import com.example.fitnesstracker.databinding.FragmentHomeBinding
import com.example.fitnesstracker.ui.viewmodel.GoalViewModel
import com.example.fitnesstracker.ui.viewmodel.ProfileViewModel
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
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val goalViewModel: GoalViewModel by activityViewModels()
    private lateinit var sessionManager: SessionManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupMap()
        observeViewModels()
        setupListeners()

        val userId = sessionManager.getUserId()
        if (userId != -1) {
            goalViewModel.fetchCurrentGoal(userId)
        }

        checkLocationPermission()
    }

    private fun setupListeners() {
        binding.buttonSetGoal.setOnClickListener {
            (activity as? MainActivity)?.showSetGoalDialog()
        }

        binding.fabAddRecordHome.setOnClickListener {
            if ((goalViewModel.goalResult.value?.data?.targetCalories ?: 0) > 0) {
                findNavController().navigate(R.id.action_homeFragment_to_addRecordFragment)
            } else {
                (activity as? MainActivity)?.showGoalRequiredDialog()
            }
        }
    }

    private fun setupMap() {
        mapView = binding.mapView
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)
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
                        val userLocation = GeoPoint(location.latitude, location.longitude)
                        updateMapLocation(userLocation)
                        findNearbyFitnessCenters(userLocation)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to get location",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }.addOnFailureListener {
                Toast.makeText(requireContext(), "Location fetch error", Toast.LENGTH_SHORT).show()
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
        mapView.overlays.add(userMarker)
        mapView.invalidate()
    }

    private fun findNearbyFitnessCenters(location: GeoPoint) {
        lifecycleScope.launch(Dispatchers.IO) {
            val poiProvider = NominatimPOIProvider(Configuration.getInstance().userAgentValue)
            try {
                val pois = poiProvider.getPOICloseTo(location, "fitness", 50, 0.1)
                withContext(Dispatchers.Main) {
                    addPoisToMap(pois)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Error finding fitness centers",
                        Toast.LENGTH_SHORT
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
            mapView.overlays.add(poiMarker)
        }
        mapView.invalidate()
    }

    private fun observeViewModels() {
        goalViewModel.goalResult.observe(viewLifecycleOwner) { response ->
            if (response != null && response.success == true) {
                val goalData = response.data
                val currentGoalTarget = goalData?.targetCalories ?: 0
                val current = goalData?.currentCalories ?: 0
                val actualPercent = goalData?.progressPercent?.toInt() ?: 0

                animateProgressBar(actualPercent.coerceAtMost(100))
                binding.textViewGoalTarget.text = "Goal: $currentGoalTarget kcal"

                if (actualPercent >= 100) {
                    binding.textViewGoalProgress.text =
                        "$current / $currentGoalTarget kcal (Goal Met! 🎉)"
                    binding.progressBarGoal.progressTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.green_success)
                    )
                } else {
                    binding.textViewGoalProgress.text =
                        "$current / $currentGoalTarget kcal ($actualPercent%)"
                    binding.progressBarGoal.progressTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.md_theme_secondary)
                    )
                }
            } else if (response != null) {
                binding.textViewGoalTarget.text = "No goal set"
                animateProgressBar(0)
                binding.textViewGoalProgress.text = "Tap Update Goal to start"
            }
        }

        profileViewModel.profile.observe(viewLifecycleOwner) { profile ->
            // This observer is for the profile prompt, no need to add UI logic here
        }

        goalViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun animateProgressBar(toProgress: Int) {
        val animation = ObjectAnimator.ofInt(
            binding.progressBarGoal,
            "progress",
            binding.progressBarGoal.progress,
            toProgress
        )
        animation.duration = 1000
        animation.interpolator = AccelerateDecelerateInterpolator()
        animation.start()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
