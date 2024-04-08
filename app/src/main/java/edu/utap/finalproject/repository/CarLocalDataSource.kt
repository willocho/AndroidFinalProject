package edu.utap.finalproject.repository

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {
    @Query("SELECT * FROM cars")
    fun getAll(): Flow<List<CarModel>>

    @Insert
    fun insert(car: CarModel)

    @Delete
    fun delete(car: CarModel)
}

@Database(entities = [CarModel::class], version = 1)
abstract class CarLocalDataSource : RoomDatabase(
) {
    abstract fun carDao(): CarDao
}