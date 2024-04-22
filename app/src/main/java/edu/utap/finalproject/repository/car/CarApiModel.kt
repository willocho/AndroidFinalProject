package edu.utap.finalproject.repository.car

data class CarApiModel(
    var documentId: String? = null,
    val licensePlate: String = "",
    val latitude: Double = 0.0,
    val longitude:Double = 0.0
)
