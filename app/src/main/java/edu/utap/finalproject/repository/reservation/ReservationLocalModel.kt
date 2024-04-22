package edu.utap.finalproject.repository.reservation

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

@Entity(tableName = "reservations")
data class ReservationLocalModel(
    @PrimaryKey
    val documentId: String,
    val userId: String,
    val startTime: Long,
    val endTime: Long,
    val car: String //Reference to a CarLocalModel
)