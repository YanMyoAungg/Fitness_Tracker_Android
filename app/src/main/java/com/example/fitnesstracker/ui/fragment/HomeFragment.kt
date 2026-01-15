package com.example.fitnesstracker.ui.fragment

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.fitnesstracker.MainActivity
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.remote.SessionManager
import com.example.fitnesstracker.databinding.FragmentHomeBinding
import com.example.fitnesstracker.ui.viewmodel.GoalViewModel
import com.example.fitnesstracker.ui.viewmodel.ProfileViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Use activityViewModels to share with MainActivity
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val goalViewModel: GoalViewModel by activityViewModels()
    private lateinit var sessionManager: SessionManager

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

        setupListeners()
        observeViewModels()

        val userId = sessionManager.getUserId()
        if (userId != -1) {
            if (sessionManager.getWeight() <= 0 || sessionManager.getHeight() <= 0) {
                checkProfileCompletion(userId)
            }
            goalViewModel.fetchCurrentGoal(userId)
        }
    }

    private fun setupListeners() {
        // FAB listener is now in MainActivity
        binding.buttonViewHistory.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_historyFragment)
        }

        binding.buttonProfile.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }

        binding.buttonSetGoal.setOnClickListener {
            (activity as? MainActivity)?.showSetGoalDialog()
        }
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
                    binding.textViewGoalProgress.text = "$current / $currentGoalTarget kcal (Goal Met! 🎉)"
                    binding.progressBarGoal.progressTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.green_success)
                    )
                } else {
                    binding.textViewGoalProgress.text = "$current / $currentGoalTarget kcal ($actualPercent%)"
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

        goalViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun animateProgressBar(toProgress: Int) {
        val animation = ObjectAnimator.ofInt(binding.progressBarGoal, "progress", binding.progressBarGoal.progress, toProgress)
        animation.duration = 1000
        animation.interpolator = AccelerateDecelerateInterpolator()
        animation.start()
    }

    private fun checkProfileCompletion(userId: Int) {
        profileViewModel.fetchProfile(userId)
        profileViewModel.profile.observe(viewLifecycleOwner) { profile ->
            if (profile != null && !isPromptShown) {
                val isWeightMissing = profile.currentWeight == null || profile.currentWeight <= 0
                val isHeightMissing = profile.height == null || profile.height <= 0

                if (isWeightMissing || isHeightMissing) {
                    isPromptShown = true
                    showProfilePrompt()
                } else {
                    sessionManager.saveBodyInfo(
                        profile.currentWeight ?: 0f,
                        profile.height ?: 0f,
                        profile.age ?: 0
                    )
                }
            }
        }
    }

    private fun showProfilePrompt() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_prompt, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnPositive = dialogView.findViewById<Button>(R.id.button_positive)
        val btnNegative = dialogView.findViewById<Button>(R.id.button_negative)

        btnPositive.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
            dialog.dismiss()
        }
        btnNegative.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
