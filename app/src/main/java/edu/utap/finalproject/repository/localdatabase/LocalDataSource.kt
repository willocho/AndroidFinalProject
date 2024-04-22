package edu.utap.finalproject.repository.localdatabase

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import edu.utap.finalproject.repository.car.CarLocalModel
import edu.utap.finalproject.repository.reservation.ReservationLocalModel
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {
    @Query("SELECT * FROM cars")
    fun getAll(): Flow<List<CarLocalModel>>

    @Insert
    fun insert(car: CarLocalModel)

    @Delete
    fun delete(car: CarLocalModel)
}

@Dao
interface ReservationDao {
    @Query("Select * from reservations as r " +
            "where r.userId = :userId")
    fun getAllReservationsForUser(userId: String): Flow<List<ReservationLocalModel>>

    @Insert
    fun insert(reservation: ReservationLocalModel)

    @Delete
    fun delete(reservation: ReservationLocalModel)

    @Query("Delete from reservations")
    fun truncate()
}

@Database(entities = [CarLocalModel::class,
    ReservationLocalModel::class],
    version = 1)
abstract class LocalDataSource : RoomDatabase(
) {
    abstract fun carDao(): CarDao
    abstract fun reservationDao(): ReservationDao
}