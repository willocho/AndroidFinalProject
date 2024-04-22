package edu.utap.finalproject.repository.car

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import edu.utap.finalproject.repository.firebase.RemoteDataSource
import edu.utap.finalproject.repository.localdatabase.CarDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CarRepository(
    carLocalDataSource: CarDao,
    remoteDataSource: RemoteDataSource
) {

    private val remoteCars: Flow<List<CarApiModel>> = callbackFlow {
        var carCollection: CollectionReference? = null
        try {
            carCollection = remoteDataSource.accessCarData()
            Log.d(javaClass.simpleName, "Remote car accessed")
        }
        catch (e: Throwable){
            Log.e(javaClass.simpleName, "$e")
            close(e)
        }
        val subscription = carCollection?.addSnapshotListener { snapshot, _ ->
            if(snapshot == null){ return@addSnapshotListener }
            try {
                val newCars: MutableList<CarApiModel> = mutableListOf()
                snapshot.documents.forEach {
                    val car = it.toObject(CarApiModel::class.java)
                    if(car != null){
                        car.documentId = it.id
                        newCars.add(car)
                    }
                }
                trySend(newCars)
                Log.d(javaClass.simpleName, "Remote car update")
            }
            catch (e: Throwable){
                Log.e(javaClass.simpleName, "$e")
                close(e)
            }
        }
        awaitClose { subscription?.remove()}
    }

    private val localCars: Flow<List<CarLocalModel>> = carLocalDataSource.getAll()
        .onEach {
            Log.d(javaClass.simpleName, "Local car update")
        }

    val cars: Flow<List<CarLocalModel>> = flow{
        Log.d(javaClass.simpleName, "Cars flow starting")
        localCars
            .combine(remoteCars) { local, remote ->
                Log.d(javaClass.simpleName, "Local cars:${local}")
                Log.d(javaClass.simpleName, "Remote cars:${remote}")
                val notInRemote = local.filter { localCar -> !remote.any { it.documentId == localCar.documentId } }
                val notInLocal = remote.filter { remoteCar -> !local.any { it.documentId == remoteCar.documentId } }
                MainScope().launch(Dispatchers.IO) {
                    //Delete cars missing from remote
                    for(deletedCar in notInRemote){
                        carLocalDataSource.delete(deletedCar)
                    }
                    //Insert cars missing from local
                    for(insertedCar in notInLocal){
                        val newCar = CarLocalModel(
                            insertedCar.documentId!!,
                            insertedCar.licensePlate,
                            insertedCar.latitude,
                            insertedCar.longitude,
                        )
                        carLocalDataSource.insert(newCar)
                    }
                }
                val inBoth = local.filter { localCar -> remote.any { it.documentId == localCar.documentId } }
                Log.d(javaClass.simpleName, "Cars in both:${inBoth}")
                if(notInLocal.isEmpty() && notInRemote.isEmpty()){
                    return@combine inBoth
                }
                else{
                    return@combine local
                }
            }
            .collect{
                emit(it)
            }
        Log.d(javaClass.simpleName, "Cars flow complete")
    }

}