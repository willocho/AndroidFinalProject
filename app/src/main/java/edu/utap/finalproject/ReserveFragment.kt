package edu.utap.finalproject

import android.os.Bundle
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
import androidx.navigation.fragment.navArgs
import edu.utap.finalproject.databinding.ReserveCarBinding
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

    private val startDate = Calendar.getInstance()
    private val endDate = Calendar.getInstance()
    private val timeFormat = SimpleDateFormat("h:mma", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dMMMyyyy", Locale.getDefault())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ReserveCarBinding.inflate(inflater, container, false)


        lifecycleScope.launch {
            viewModel.cars.collect {
                val adapter = ArrayAdapter(
                    requireContext(),
                    androidx.transition.R.layout.support_simple_spinner_dropdown_item,
                    it.map { car -> car.licensePlate }
                )
                binding.reserveSpinner.adapter = adapter
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

            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}