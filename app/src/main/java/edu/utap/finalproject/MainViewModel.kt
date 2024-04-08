package edu.utap.finalproject

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import edu.utap.finalproject.repository.CarLocalDataSource
import edu.utap.finalproject.repository.CarModel
import edu.utap.finalproject.repository.CarRemoteDataSource
import edu.utap.finalproject.repository.CarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MainViewModel(
    carRepository: CarRepository
) : ViewModel() {

    val cars = carRepository.cars

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
                    CarLocalDataSource::class.java,
                    "cars"
                    ).build()
                val firestore = Firebase.firestore
                val remoteDb = CarRemoteDataSource(firestore)
                val repository = CarRepository(localDb.carDao(), remoteDb)
                Log.d(javaClass.simpleName, "Viewmodel created")
                return (MainViewModel(repository)) as T
            }
        }
    }
}