package com.example.fitnesstracker.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.fitnesstracker.data.model.ActivityType
import com.example.fitnesstracker.data.remote.SessionManager
import com.example.fitnesstracker.databinding.FragmentAddRecordBinding
import com.example.fitnesstracker.ui.viewmodel.FitnessViewModel
import java.text.SimpleDateFormat
import java.util.*

class AddRecordFragment : Fragment() {

    private var _binding: FragmentAddRecordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FitnessViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

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

        setupSpinner()

        binding.buttonSave.setOnClickListener {
            // Get the selected ActivityType object
            val selectedDisplayName = binding.spinnerActivityType.selectedItem.toString()
            val selectedType = ActivityType.values().find { it.displayName == selectedDisplayName }
            
            // Use databaseValue (e.g. "jumping_rope") instead of displayName (e.g. "Jumping Rope")
            val typeKey = selectedType?.databaseValue ?: selectedDisplayName

            val duration = binding.editTextDuration.text.toString().toIntOrNull() ?: 0
            val calories = binding.editTextCalories.text.toString().toIntOrNull() ?: 0
            
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val currentDateTime = sdf.format(Date())

            val userId = sessionManager.getUserId()

            if (userId != -1) {
                if (duration > 0 && calories > 0) {
                    viewModel.addRecord(userId, typeKey, duration, calories, currentDateTime)
                } else {
                    Toast.makeText(requireContext(), "Please enter duration and calories", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "User session expired. Please log in again.", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.addRecordResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Activity saved successfully!", Toast.LENGTH_SHORT).show()
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
