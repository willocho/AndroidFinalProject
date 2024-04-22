package edu.utap.finalproject

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import edu.utap.finalproject.repository.car.CarLocalModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MapsFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private val carMarkers: MutableMap<String, Marker> = mutableMapOf()

    private class MapInfoWindowAdapter(
        private val layoutInflater: LayoutInflater,
    ) : GoogleMap.InfoWindowAdapter {

        override fun getInfoContents(p0: Marker): View? {
            val layout = layoutInflater.inflate(R.layout.map_marker, null)
            val associatedCar: CarLocalModel = p0.tag as CarLocalModel
            layout.findViewById<TextView>(R.id.markerLicensePlateTV).text = associatedCar.licensePlate
            return layout
        }

        override fun getInfoWindow(p0: Marker): View? {
            return null
        }

    }

    private val callback = OnMapReadyCallback { googleMap ->
        val MADISON = LatLng(43.073, -89.401)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MADISON, 15.0f))
        googleMap.setInfoWindowAdapter(MapInfoWindowAdapter(layoutInflater))
        googleMap.setOnInfoWindowClickListener {
            val associatedCar: CarLocalModel = it.tag as CarLocalModel
            Log.d(javaClass.simpleName, "Clicked on maker for car ${associatedCar.licensePlate}/${associatedCar.documentId}")
            val directions = MapsFragmentDirections.actionToReserve(associatedCar)
            findNavController().navigate(directions)
        }
        MainScope().launch {
            viewModel.cars.collect { cars ->
                for(car in cars){
                    //If this is a new car
                    if(!carMarkers.keys.any { it == car.documentId }){
                        val carLatLng = LatLng(car.latitude, car.longitude)
                        val marker = googleMap
                            .addMarker(MarkerOptions().position(carLatLng))
                        if(marker != null) {
                            marker.tag = car
                            carMarkers[car.documentId] = marker
                        }
                    }
                }
                //Remove removed cars
                carMarkers.filter { (key, _) -> !cars.any{ car ->  key == car.documentId} }
                    .forEach { (key, value) ->
                        value.remove()
                        carMarkers.remove(key)
                    }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
}