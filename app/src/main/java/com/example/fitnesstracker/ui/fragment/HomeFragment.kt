package com.example.fitnesstracker.ui.fragment

import android.content.res.ColorStateList
import android.graphics.Color
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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.model.FitnessRecord
import com.example.fitnesstracker.data.remote.SessionManager
import com.example.fitnesstracker.databinding.FragmentHomeBinding
import com.example.fitnesstracker.ui.viewmodel.FitnessViewModel
import com.example.fitnesstracker.ui.viewmodel.GoalViewModel
import com.example.fitnesstracker.ui.viewmodel.ProfileViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val goalViewModel: GoalViewModel by activityViewModels()
    private val fitnessViewModel: FitnessViewModel by activityViewModels()
    private lateinit var sessionManager: SessionManager
    private lateinit var barChart: BarChart

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
        barChart = binding.barChart

        setupListeners()
        observeViewModels()
    }

    override fun onResume() {
        super.onResume()
        // Fetch data every time the fragment is resumed to ensure it's up-to-date
        val userId = sessionManager.getUserId()
        if (userId != -1) {
            if (sessionManager.getWeight() <= 0 || sessionManager.getHeight() <= 0) {
                checkProfileCompletion(userId)
            }
            goalViewModel.fetchCurrentGoal(userId)
            // Fetch last 7 days of data for the chart
            val calendar = Calendar.getInstance()
            val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, -6)
            val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            fitnessViewModel.fetchHistory(userId, startDate, endDate)
        }
    }

    private fun setupListeners() {
        binding.buttonAddRecord.setOnClickListener {
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
                currentGoalTarget = goalData?.targetCalories ?: 0
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

        fitnessViewModel.history.observe(viewLifecycleOwner) { history ->
            if (history != null) {
                setupBarChart(history)
            } else {
                setupBarChart(emptyList())
            }
        }

        goalViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBarChart(records: List<FitnessRecord>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())

        val dailyTotals = records.groupBy { it.activityDate.substring(0, 10) }
            .mapValues { it.value.sumOf { rec -> rec.caloriesBurned } }

        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance()
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dayCal.time)
            val calories = dailyTotals[dateKey] ?: 0
            entries.add(BarEntry((6 - i).toFloat(), calories.toFloat()))
            labels.add(sdf.format(dayCal.time))
        }

        val dataSet = BarDataSet(entries, "Calories Burned")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.blue)
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 10f

        val barData = BarData(dataSet)
        barChart.data = barData

        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setDrawGridBackground(false)

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisRight.isEnabled = false

        barChart.animateY(1000)
        barChart.invalidate()
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
