package com.androiddevs.runningappyt.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.data.other.TrackingUtility
import com.androiddevs.runningappyt.databinding.ItemRunBinding
import com.androiddevs.runningappyt.domain.model.RunDomain
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<RunDomain>() {
        override fun areItemsTheSame(oldItem: RunDomain, newItem: RunDomain): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RunDomain, newItem: RunDomain): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemRunBinding.inflate(inflater, parent, false)
        return RunViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = differ.currentList[position]

        holder.bind(run)
    }

    fun submitList(list: List<RunDomain>) = differ.submitList(list)


    inner class RunViewHolder(private val binding: ItemRunBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(run: RunDomain) {
            binding.apply {
                Glide.with(itemView).load(run.img).into(ivRunImage)

                val calendar = Calendar.getInstance().apply {
                    timeInMillis = run.timestamp
                }
                val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())

                tvDate.text = dateFormat.format(calendar.time)

                val avgSpeed = "${run.avgSpeedInKMH}km/h"
                tvAvgSpeed.text = avgSpeed

                val distanceInKm = "${run.distanceInMeters / 1000f}km"
                tvDistance.text = distanceInKm

                tvTime.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

                val caloriesBurned = "${run.caloriesBurned}kcal"
                tvCalories.text = caloriesBurned
            }
        }
    }
}




