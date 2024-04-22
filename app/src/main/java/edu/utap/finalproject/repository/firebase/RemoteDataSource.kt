package edu.utap.finalproject.repository.firebase

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import edu.utap.finalproject.repository.reservation.ReservationLocalModel

class RemoteDataSource(
    private val db: FirebaseFirestore
) {
    fun accessCarData(): CollectionReference {
        return db.collection("cars")
    }

    fun accessReservationData(userId: String): CollectionReference {
        return db.collection("user_data/$userId/reservations")
    }

    fun sendReservation(reservation: ReservationLocalModel) {
        val collectionRef = db.collection("user_data/${reservation.userId}/reservations")

    }
}