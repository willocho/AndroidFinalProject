package edu.utap.finalproject.repository.reservation

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import java.util.Date

data class ReservationApiModel(
    var documentId: String? = null,
    val startTime: Timestamp = Timestamp(Date(0)),
    val endTime: Timestamp = Timestamp(Date(0)),
    val car: DocumentReference? = null,
)

data class ReservationApiModelOutgoing(
    val startTime: Timestamp = Timestamp(Date(0)),
    val endTime: Timestamp = Timestamp(Date(0)),
    val car: DocumentReference? = null
)