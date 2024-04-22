package edu.utap.finalproject

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.compose.material3.DatePicker
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavArgs
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import edu.utap.finalproject.databinding.ReserveCarBinding
import edu.utap.finalproject.repository.car.CarLocalModel
import edu.utap.finalproject.repository.reservation.ReservationApiModelOutgoing
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ReserveFragment : Fragment() {

    private var _binding: ReserveCarBinding? = null
    private val binding get() = _binding!!

    private val navArgs: ReserveFragmentArgs by navArgs()
    private val viewModel: MainViewModel by activityViewModels()
    private var cars: List<CarLocalModel> = listOf()

    private val startDate = Calendar.getInstance()
    private val endDate = Calendar.getInstance()
    private val timeFormat = SimpleDateFormat("h:mma", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dMMMyyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ReserveCarBinding.inflate(inflater, container, false)

        lifecycleScope.launch {
            viewModel.cars.collect {
                cars = it

                val adapter = ArrayAdapter(
                    requireContext(),
                    androidx.transition.R.layout.support_simple_spinner_dropdown_item,
                    it.map { car -> car.licensePlate }
                )


                binding.reserveSpinner.adapter = adapter

                if(navArgs.car != null){
                    val index = cars.indexOfFirst { car -> car.documentId == navArgs!!.car!!.documentId }
                    binding.reserveSpinner.setSelection(index)
                    Log.d(javaClass.simpleName, "Selected car at index $index")
                }
                else{
                    Log.d(javaClass.simpleName, "Car parameter was null!")
                }
            }
        }

        binding.reservationStartTimeTV.text = timeFormat.format(startDate.time)
        binding.reservationStartDateTV.text = dateFormat.format(startDate.time)
        binding.reservationEndTimeTV.text = timeFormat.format(endDate.time)
        binding.reservationEndDateTV.text = dateFormat.format(endDate.time)

        binding.reservationStartTimeTV.setOnClickListener {
            TimePickerFragment{ hour, minute ->
                startDate.set(Calendar.HOUR_OF_DAY, hour)
                startDate.set(Calendar.MINUTE, minute)
                binding.reservationStartTimeTV.text = timeFormat.format(startDate.time)
            }.show(childFragmentManager, "startTimePicker")
        }

        binding.reservationStartDateTV.setOnClickListener {
            DatePickerFragment { year, month, day ->
                startDate.set(year, month, day)
                binding.reservationStartDateTV.text = dateFormat.format(startDate.time)
            }.show(childFragmentManager, "startDatePicker")
        }

        binding.reservationEndTimeTV.setOnClickListener {
            TimePickerFragment{ hour, minute ->
                endDate.set(Calendar.HOUR_OF_DAY, hour)
                endDate.set(Calendar.MINUTE, minute)
                binding.reservationEndTimeTV.text = timeFormat.format(endDate.time)
            }.show(childFragmentManager, "endTimePicker")
        }

        binding.reservationEndDateTV.setOnClickListener {
            DatePickerFragment { year, month, day ->
                endDate.set(year, month, day)
                binding.reservationEndDateTV.text = dateFormat.format(endDate.time)
            }.show(childFragmentManager, "endDatePicker")
        }

        binding.submitButton.setOnClickListener {
            if(endDate.before(startDate) || endDate.equals(startDate)){
                Toast.makeText(context, "End time before start time", Toast.LENGTH_SHORT).show()
            }
            else{
                binding.submitButton.isEnabled = false
                val selectedCar = binding.reserveSpinner.selectedItem
                val car = cars.filter {
                    it.licensePlate == selectedCar
                }[0]
                val outgoingRequest = ReservationApiModelOutgoing(
                    Timestamp(startDate.time),
                    Timestamp(endDate.time),
                    viewModel.carIdToReference(car.documentId)
                )
                val request = viewModel.sendReservationRequest(outgoingRequest)
                request.addOnSuccessListener {
                    Toast.makeText(context, "Reservation request successful", Toast.LENGTH_SHORT).show()
                    val directions = ReserveFragmentDirections.actionToReservations()
                    findNavController().navigate(directions)
                }
                request.addOnFailureListener {
                    Toast.makeText(context, "Reservation request failed", Toast.LENGTH_SHORT).show()
                    binding.submitButton.isEnabled = true
                }
            }
        }

        return binding.root
    }
}