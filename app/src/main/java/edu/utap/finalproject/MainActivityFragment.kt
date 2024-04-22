package edu.utap.finalproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import edu.utap.finalproject.databinding.FragmentMainActivityBinding

class MainActivityFragment : Fragment() {
    private var _binding: FragmentMainActivityBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainActivityBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.navigationRail.setOnItemSelectedListener {
            val navController = binding.nestedActivityFragment.findNavController()
            when(it.itemId) {
                R.id.mapNavItem -> {
                    val directions = ReservationsFragmentDirections.actionToMap()
                    navController.navigate(directions)
                    true
                }
                R.id.scheduleNavItem -> {
                    val directions = MapsFragmentDirections.actionToReservations()
                    navController.navigate(directions)
                    true
                }
                R.id.reserveNavItem -> {
                    val directions = ReserveFragmentDirections.actionToReserve(null)
                    navController.navigate(directions)
                    true
                }
                else -> false
            }
        }
    }
}