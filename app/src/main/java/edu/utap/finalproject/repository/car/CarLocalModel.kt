package edu.utap.finalproject.repository.car

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "cars")
data class CarLocalModel(
    @PrimaryKey() val documentId: String,
    @ColumnInfo(name = "licensePlate") val licensePlate: String,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
) : Serializable
