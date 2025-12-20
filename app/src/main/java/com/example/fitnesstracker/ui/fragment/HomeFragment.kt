package com.example.fitnesstracker.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.remote.SessionManager
import com.example.fitnesstracker.databinding.FragmentHomeBinding
import com.example.fitnesstracker.ui.viewmodel.ProfileViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    
    // Flag to prevent the prompt from showing multiple times in one go
    private var isPromptShown = false

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

        // 1. Check if we already have info in SessionManager
        if (sessionManager.getWeight() <= 0 || sessionManager.getHeight() <= 0) {
            checkProfileCompletion()
        }

        binding.buttonAddRecord.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addRecordFragment)
        }

        binding.buttonViewHistory.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_historyFragment)
        }

        binding.buttonProfile.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
    }

    private fun checkProfileCompletion() {
        val userId = sessionManager.getUserId()
        if (userId != -1) {
            profileViewModel.fetchProfile(userId)
            
            profileViewModel.profile.observe(viewLifecycleOwner) { profile ->
                if (profile != null && !isPromptShown) {
                    // 2. Check for null or 0 values
                    val isWeightMissing = profile.currentWeight == null || profile.currentWeight <= 0
                    val isHeightMissing = profile.height == null || profile.height <= 0
                    
                    if (isWeightMissing || isHeightMissing) {
                        isPromptShown = true
                        showProfilePrompt()
                    } else {
                        // Profile is actually complete, update local session just in case
                        sessionManager.saveBodyInfo(
                            profile.currentWeight ?: 0f,
                            profile.height ?: 0f,
                            profile.age ?: 0
                        )
                    }
                }
            }
        }
    }

    private fun showProfilePrompt() {
        AlertDialog.Builder(requireContext())
            .setTitle("Complete Your Profile")
            .setMessage("Please set your height and weight to get accurate fitness tracking.")
            .setCancelable(false) // Prevent dismissing by clicking outside
            .setPositiveButton("Set Now") { _, _ ->
                findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
            }
            .setNegativeButton("Later") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
