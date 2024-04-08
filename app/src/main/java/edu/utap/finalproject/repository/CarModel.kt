package edu.utap.finalproject.repository

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cars")
data class CarModel(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "licensePlate") val licensePlate: String,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
)
