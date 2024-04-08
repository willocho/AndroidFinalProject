package edu.utap.finalproject.repository

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

class CarRemoteDataSource(
    private val db: FirebaseFirestore
) {

    fun getData(onSuccess: (List<CarApiModel>) -> Unit) {
        db.collection("cars")
            .get()
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "Retrieved ")
                val remoteCars: MutableList<CarApiModel> = mutableListOf()
                for (document in it.documents) {
                    val car = document.toObject<CarApiModel>()!!
                    remoteCars.add(car)
                }
                onSuccess(remoteCars)
            }
    }

    fun accessData(): CollectionReference {
        return db.collection("cars")
    }

}