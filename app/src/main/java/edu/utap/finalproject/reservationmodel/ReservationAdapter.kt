package edu.utap.finalproject.reservationmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.utap.finalproject.MainViewModel
import edu.utap.finalproject.databinding.ReservationItemBinding
import edu.utap.finalproject.repository.car.CarLocalModel
import edu.utap.finalproject.repository.reservation.ReservationLocalModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ReservationAdapter(
    private val viewModel: MainViewModel,
    private val onClick: (ReservationLocalModel) -> Unit
) :
    ListAdapter<ReservationLocalModel, ReservationAdapter.ReservationViewHolder>(ReservationDiffCallback) {

    private val simpleFormat = SimpleDateFormat("MMM-dd h:mma", Locale.US)
    private var cars: List<CarLocalModel> = listOf()
    private var job: Job = MainScope().launch {
        viewModel.cars.collect {
            cars = it
        }
    }

    class ReservationViewHolder(binding: ReservationItemBinding, val onClick: (ReservationLocalModel) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {

        val idTv = binding.reservationIdTV
        val startTimeTV = binding.reservationStartTimeTV
        val endTimeTV = binding.reservationEndTimeTV
        var currentReservation: ReservationLocalModel? = null

        init {
            binding.root.setOnClickListener{
                currentReservation?.let {
                    onClick(it)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val view = ReservationItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ReservationViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        val reservation = getItem(position)
        val car = cars.filter { it.documentId == reservation.car }[0]
        holder.idTv.text = car.licensePlate
        holder.startTimeTV.text = simpleFormat.format(reservation.startTime).toString()
        holder.endTimeTV.text = simpleFormat.format(reservation.endTime).toString()
        holder.currentReservation = reservation
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        job.cancel()
        super.onDetachedFromRecyclerView(recyclerView)
    }
}

object ReservationDiffCallback : DiffUtil.ItemCallback<ReservationLocalModel>() {
    override fun areContentsTheSame(oldItem: ReservationLocalModel, newItem: ReservationLocalModel): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: ReservationLocalModel, newItem: ReservationLocalModel): Boolean {
        return oldItem.documentId == newItem.documentId
    }
}