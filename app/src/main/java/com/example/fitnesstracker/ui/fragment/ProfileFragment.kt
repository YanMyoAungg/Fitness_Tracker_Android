package com.example.fitnesstracker.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.remote.SessionManager
import com.example.fitnesstracker.databinding.FragmentProfileBinding
import com.example.fitnesstracker.ui.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        val userId = sessionManager.getUserId()
        if (userId != -1) {
            viewModel.fetchProfile(userId)
        }

        binding.buttonEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        binding.buttonLogout.setOnClickListener {
            sessionManager.logout()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                binding.textViewUsername.text = "Username: ${it.username}"
                binding.textViewEmail.text = "Email: ${it.email}"
                binding.textViewPhone.text = "Phone: ${it.phone ?: "Not set"}"
                binding.textViewWeightHeight.text = "Weight: ${it.currentWeight?.toInt() ?: 0}kg | Height: ${it.height?.toInt() ?: 0}cm"
                binding.textViewDOBGender.text = "DOB: ${it.dateOfBirth ?: "Not set"} | Gender: ${it.gender ?: "Not set"}"
                
                // Keep local session updated for fitness record calculations
                sessionManager.saveBodyInfo(
                    it.currentWeight ?: 0f,
                    it.height ?: 0f,
                    it.age ?: 0
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
