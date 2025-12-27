package com.example.fitnesstracker.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstracker.data.model.FitnessRecord
import com.example.fitnesstracker.databinding.ItemFitnessRecordBinding
import java.text.SimpleDateFormat
import java.util.Locale

class FitnessAdapter(private var records: List<FitnessRecord>) :
    RecyclerView.Adapter<FitnessAdapter.FitnessViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FitnessViewHolder {
        val binding = ItemFitnessRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FitnessViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FitnessViewHolder, position: Int) {
        holder.bind(records[position])
    }

    override fun getItemCount(): Int = records.size

    fun updateData(newRecords: List<FitnessRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }

    class FitnessViewHolder(private val binding: ItemFitnessRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: FitnessRecord) {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val dateObj = inputFormat.parse(record.activityDate)

                if (dateObj != null) {
                    val dateFormat = SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault())
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                    binding.textViewDate.text = dateFormat.format(dateObj)
                    binding.textViewTime.text = timeFormat.format(dateObj)
                } else {
                    binding.textViewDate.text = record.activityDate
                    binding.textViewTime.text = ""
                }
            } catch (e: Exception) {
                binding.textViewDate.text = record.activityDate
                binding.textViewTime.text = ""
            }

            binding.textViewActivityType.text = record.activityType
            binding.textViewCalories.text = "${record.caloriesBurned} kcal"
            binding.textViewDuration.text = "${record.duration} min"
            
            // Show location
            binding.textViewLocation.text = if (!record.locationName.isNullOrEmpty()) {
                "Location: ${record.locationName}"
            } else {
                "Location: Unknown"
            }
        }
    }
}
