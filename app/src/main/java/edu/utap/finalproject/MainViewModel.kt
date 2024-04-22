package edu.utap.finalproject

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.room.Room
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import edu.utap.finalproject.repository.localdatabase.LocalDataSource
import edu.utap.finalproject.repository.firebase.RemoteDataSource
import edu.utap.finalproject.repository.car.CarRepository
import edu.utap.finalproject.repository.reservation.ReservationApiModelOutgoing
import edu.utap.finalproject.repository.reservation.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(
    carRepository: CarRepository,
    reservationRepository: ReservationRepository,
) : ViewModel() {

    val cars = carRepository.cars
    val reservations = reservationRepository.reservations
    private val _showToolbar = MutableStateFlow(false)
    val showToolbar: StateFlow<Boolean> = _showToolbar.asStateFlow()

    fun setShowToolbar(value: Boolean){
        Log.d(javaClass.simpleName, "Changing toolbar from ${_showToolbar.value} to $value")
        _showToolbar.value = value
    }

    fun carIdToReference(carId: String): DocumentReference {
        val firebase = Firebase.firestore
        return firebase.document("/cars/$carId")
    }

    fun sendReservationRequest(request: ReservationApiModelOutgoing): Task<DocumentReference> {
        val firebase = Firebase.firestore
        val auth = Firebase.auth
        val collection = firebase.collection("/user_data/${auth.currentUser!!.uid}/reservations")
        val doc = collection.add(request)
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "The request succeeded!")
            }
        return doc
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory{
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                Log.d(javaClass.simpleName, "Creating viewmodel")
                val localDb = Room.databaseBuilder(
                    extras[APPLICATION_KEY]!!.applicationContext,
                    LocalDataSource::class.java,
                    "car_share_db"
                    ).build()
                val firestore = Firebase.firestore
                val remoteDb = RemoteDataSource(firestore)
                val carRepository = CarRepository(localDb.carDao(), remoteDb)
                val reservationRepository = ReservationRepository(
                    Firebase.auth,
                    localDb.reservationDao(),
                    remoteDb)
                Log.d(javaClass.simpleName, "Viewmodel created")
                return (MainViewModel(carRepository, reservationRepository)) as T
            }
        }
    }
}