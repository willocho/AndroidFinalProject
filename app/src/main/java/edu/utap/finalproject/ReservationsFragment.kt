package edu.utap.finalproject

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import edu.utap.finalproject.databinding.FragmentReservationsBinding
import edu.utap.finalproject.reservationmodel.ReservationAdapter
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ReservationsFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    private var _binding: FragmentReservationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentReservationsBinding.inflate(inflater)

        val reservationAdapter = ReservationAdapter(viewModel){
            Log.d(javaClass.simpleName, "${it}")
        }

        binding.upcomingReservationRV.adapter = reservationAdapter

        MainScope().launch {
            viewModel.reservations.collect{
                reservationAdapter.submitList(it)
            }
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}