package com.example.fitnesstracker.ui.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.fitnesstracker.data.remote.SessionManager
import com.example.fitnesstracker.databinding.FragmentEditProfileBinding
import com.example.fitnesstracker.ui.viewmodel.ProfileViewModel
import java.util.*

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        val userId = sessionManager.getUserId()
        if (userId != -1) {
            viewModel.fetchProfile(userId)
        }

        binding.editTextDOB.setOnClickListener {
            showDatePicker()
        }

        binding.buttonSaveProfile.setOnClickListener {
            saveProfile()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                binding.editTextPhone.setText(it.phone)
                binding.editTextWeight.setText(it.currentWeight?.toString())
                binding.editTextHeight.setText(it.height?.toString())
                binding.editTextDOB.setText(it.dateOfBirth)
                when (it.gender) {
                    "Male" -> binding.radioButtonMale.isChecked = true
                    "Female" -> binding.radioButtonFemale.isChecked = true
                    "Other" -> binding.radioButtonOther.isChecked = true
                }
            }
        }

        viewModel.updateResult.observe(viewLifecycleOwner) { response ->
            if (response.success) {
                Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(context, response.message ?: "Update failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveProfile() {
        val userId = sessionManager.getUserId()
        val phone = binding.editTextPhone.text.toString()
        val weight = binding.editTextWeight.text.toString().toFloatOrNull()
        val height = binding.editTextHeight.text.toString().toFloatOrNull()
        val dob = binding.editTextDOB.text.toString()
        val gender = when {
            binding.radioButtonMale.isChecked -> "Male"
            binding.radioButtonFemale.isChecked -> "Female"
            binding.radioButtonOther.isChecked -> "Other"
            else -> null
        }

        if (userId != -1) {
            viewModel.updateProfile(userId, height, weight, dob, gender, phone)
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                binding.editTextDOB.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
