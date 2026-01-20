package com.example.fitnesstracker.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.remote.SessionManager
import com.example.fitnesstracker.databinding.FragmentProfileBinding
import com.example.fitnesstracker.ui.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by activityViewModels()
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
            profileViewModel.fetchProfile(userId)
        }

        observeViewModel()


        binding.buttonEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }
    }

    private fun observeViewModel() {
        profileViewModel.profile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                binding.textViewUsername.text = it.username
                binding.textViewEmail.text = it.email

                binding.textViewWeight.text = "${it.currentWeight ?: "N/A"}kg\nWeight"
                binding.textViewHeight.text = "${it.height ?: "N/A"}cm\nHeight"
                binding.textViewAge.text = "${it.age ?: "N/A"}\nAge"

                // Create and set the profile icon
                val initial = it.username.first().uppercaseChar()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
