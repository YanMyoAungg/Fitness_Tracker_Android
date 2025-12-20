package com.example.fitnesstracker.ui.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnesstracker.data.remote.SessionManager
import com.example.fitnesstracker.databinding.FragmentHistoryBinding
import com.example.fitnesstracker.ui.FitnessAdapter
import com.example.fitnesstracker.ui.viewmodel.FitnessViewModel
import java.util.*

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FitnessViewModel by viewModels()
    private lateinit var adapter: FitnessAdapter
    private lateinit var sessionManager: SessionManager

    private var startDate: String? = null
    private var endDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        setupRecyclerView()
        setupFilters()

        viewModel.history.observe(viewLifecycleOwner) { records ->
            binding.progressBarHistory.isVisible = false
            adapter.updateData(records)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.progressBarHistory.isVisible = false
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            }
        }

        refreshHistory()
    }

    private fun setupRecyclerView() {
        adapter = FitnessAdapter(emptyList())
        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HistoryFragment.adapter
        }
    }

    private fun setupFilters() {
        binding.buttonStartDate.setOnClickListener {
            showDatePicker { date ->
                startDate = date
                binding.buttonStartDate.text = date
                refreshHistory()
            }
        }

        binding.buttonEndDate.setOnClickListener {
            showDatePicker { date ->
                endDate = date
                binding.buttonEndDate.text = date
                refreshHistory()
            }
        }

        binding.buttonClearFilter.setOnClickListener {
            startDate = null
            endDate = null
            binding.buttonStartDate.text = "Start Date"
            binding.buttonEndDate.text = "End Date"
            refreshHistory()
        }
    }

    private fun refreshHistory() {
        val userId = sessionManager.getUserId()
        if (userId != -1) {
            binding.progressBarHistory.isVisible = true
            viewModel.fetchHistory(userId, startDate, endDate)
        } else {
            Toast.makeText(requireContext(), "Session expired. Please log in.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                onDateSelected(formattedDate)
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
