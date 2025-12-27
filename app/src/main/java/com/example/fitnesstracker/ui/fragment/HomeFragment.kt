package com.example.fitnesstracker.ui.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.remote.SessionManager
import com.example.fitnesstracker.databinding.FragmentHomeBinding
import com.example.fitnesstracker.ui.viewmodel.GoalViewModel
import com.example.fitnesstracker.ui.viewmodel.ProfileViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModels()
    private val goalViewModel: GoalViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    private var isPromptShown = false
    private var currentGoalTarget: Int = 0

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
        binding.buttonAddRecord.setOnClickListener {
            // ENFORCE RULE: Check if goal is set before allowing record entry
            if (currentGoalTarget > 0) {
                findNavController().navigate(R.id.action_homeFragment_to_addRecordFragment)
            } else {
                showGoalRequiredDialog()
            }
        }

        binding.buttonViewHistory.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_historyFragment)
        }

        binding.buttonProfile.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }

        binding.buttonSetGoal.setOnClickListener {
            showSetGoalDialog()
        }
    }

    private fun observeViewModels() {
        goalViewModel.goalResult.observe(viewLifecycleOwner) { response ->
            if (response != null && response.success == true) {
                val goalData = response.data
                currentGoalTarget = goalData?.targetCalories ?: 0 // Update our tracking variable
                val current = goalData?.currentCalories ?: 0
                val actualPercent = goalData?.progressPercent?.toInt() ?: 0

                binding.progressBarGoal.progress = actualPercent.coerceAtMost(100)
                binding.textViewGoalTarget.text = "Goal: $currentGoalTarget kcal"

                if (actualPercent >= 100) {
                    binding.textViewGoalProgress.text = "$current / $currentGoalTarget kcal (Goal Met! 🎉)"
                    binding.progressBarGoal.progressTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.green)
                    )
                } else {
                    binding.textViewGoalProgress.text = "$current / $currentGoalTarget kcal ($actualPercent%)"
                    binding.progressBarGoal.progressTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.blue)
                    )
                }
            } else if (response != null) {
                currentGoalTarget = 0
                binding.textViewGoalTarget.text = "No goal set"
                binding.progressBarGoal.progress = 0
                binding.textViewGoalProgress.text = "Tap Update Goal to start"
            }
        }

        goalViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showGoalRequiredDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Weekly Goal Required")
            .setMessage("You need to set a weekly calorie goal before you can log activities.")
            .setPositiveButton("Set Goal Now") { _, _ ->
                showSetGoalDialog()
            }
            .setNegativeButton("Cancel", null)
            .show()
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

    private fun showSetGoalDialog() {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "e.g. 2000"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Set Weekly Calorie Goal")
            .setMessage("Enter your target calories for this week:")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val target = input.text.toString().toIntOrNull()
                if (target != null && target > 0) {
                    val userId = sessionManager.getUserId()
                    goalViewModel.setGoal(userId, target)
                } else {
                    Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showProfilePrompt() {
        AlertDialog.Builder(requireContext())
            .setTitle("Complete Your Profile")
            .setMessage("Please set your height and weight to get accurate fitness tracking.")
            .setCancelable(false)
            .setPositiveButton("Set Now") { _, _ ->
                findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
            }
            .setNegativeButton("Later", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
