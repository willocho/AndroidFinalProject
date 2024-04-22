package edu.utap.finalproject.repository.reservation

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ListenerRegistration
import edu.utap.finalproject.repository.car.CarApiModel
import edu.utap.finalproject.repository.car.CarLocalModel
import edu.utap.finalproject.repository.firebase.RemoteDataSource
import edu.utap.finalproject.repository.localdatabase.ReservationDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class ReservationRepository(
    private val firebaseAuth: FirebaseAuth,
    private val reservationLocalDatasource: ReservationDao,
    private val remoteDataSource: RemoteDataSource
) {

    private val remoteReservations: Flow<List<ReservationApiModel>> = callbackFlow {
        var subscription: ListenerRegistration? = null
        val authUpdateListener: (FirebaseAuth) -> Unit = {
            subscription?.remove()
            var reservationCollection: CollectionReference? = null
            try {
                if(firebaseAuth.currentUser != null){
                    reservationCollection = remoteDataSource
                        .accessReservationData(firebaseAuth.currentUser!!.uid)
                    Log.d(javaClass.simpleName, "Remove reservations accessed")
                }
            }
            catch (e: Exception){
                Log.e(javaClass.simpleName, "Reservation error: $e")
                close(e)
            }
            subscription = reservationCollection?.addSnapshotListener { snapshot, e ->
               if(snapshot == null){ return@addSnapshotListener }
                try{
                    val reservations: MutableList<ReservationApiModel> = mutableListOf()
                    Log.d(javaClass.simpleName, "Remote reservation update: $reservations")
                    snapshot.documents.forEach {
                        val newReservation = it.toObject(ReservationApiModel::class.java)
                        if(newReservation != null){
                            newReservation.documentId = it.id
                            reservations.add(newReservation)
                        }
                    }
                    trySend(reservations)
                }
               catch (e: Throwable){
                   Log.e(javaClass.simpleName, "Reservation error: $e")
                   close(e)
               }
            }
        }
        firebaseAuth.addAuthStateListener(authUpdateListener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(authUpdateListener)
        }
    }

    private val localReservations = callbackFlow{
        var job: Job? = null
        val authUpdateListener: (FirebaseAuth) -> Unit = {
            job?.cancel()
            Log.d(javaClass.simpleName, "Res user updated to ${it.uid}")
            if(it.currentUser != null){
                val localRes = reservationLocalDatasource
                    .getAllReservationsForUser(it.uid!!)
                job = launch {
                    localRes.collect { reservations ->
                        Log.d(javaClass.simpleName, "Sending $reservations")
                        trySend(reservations)
                    }
                }
            }
            else{
                //Send blank list if no users
                trySend(listOf())
                Log.d(javaClass.simpleName, "Sending empty list")
                //Also truncate the database
                launch(Dispatchers.IO) {
                    reservationLocalDatasource.truncate()
                }
            }
        }
        firebaseAuth.addAuthStateListener(authUpdateListener)
        awaitClose {
            job?.cancel()
            firebaseAuth.removeAuthStateListener(authUpdateListener)
        }
    }

    val reservations: Flow<List<ReservationLocalModel>> = localReservations
        .combineTransform(remoteReservations) { local, remote ->
            val notInRemote = local.filter { localRes -> !remote.any { (it.documentId != null) && (it.documentId == localRes.documentId) } }
            val notInLocal = remote.filter { remoteRes -> !local.any { (remoteRes.documentId != null) && (it.documentId == remoteRes.documentId) } }
            MainScope().launch(Dispatchers.IO) {
                //Delete reservations missing from remote
                for(deletedReservation in notInRemote){
                    reservationLocalDatasource.delete(deletedReservation)
                }
                //Insert cars missing from local
                for(insertedRes in notInLocal){
                    Log.d(javaClass.simpleName, "Remote is $insertedRes")
                    val car = insertedRes.car!!.get().await()
                    val carRemote = car.toObject(CarApiModel::class.java)
                    carRemote?.documentId = car.id
                    Log.d(javaClass.simpleName, "Remote car: $carRemote")
                    if(carRemote != null && firebaseAuth.uid != null){
                        val newRes = ReservationLocalModel(
                            insertedRes.documentId!!,
                            firebaseAuth.uid!!,
                            insertedRes.startTime.toDate().time,
                            insertedRes.endTime.toDate().time,
                            carRemote.documentId!!
                        )
                        Log.d(javaClass.simpleName, "Trying to insert $newRes")
                        try {
                            reservationLocalDatasource.insert(newRes)
                        }
                        catch (e: SQLiteConstraintException){
                            continue
                        }
                    }
                }
            }
            val inBoth = local.filter { localRes -> remote.any { it.documentId == localRes.documentId } }
            Log.d(javaClass.simpleName, "Reservations in both:${inBoth}")
            if(notInLocal.isEmpty() && notInRemote.isEmpty()){
                emit(inBoth)
            }
            else{
                emit(local)
            }
        }

}