package com.example.neversnooze

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarm_table")
    fun getAll(): Flow<List<AlarmEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: AlarmEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(alarms: List<AlarmEntity>) // Bulk insert method added

    @Delete
    suspend fun delete(alarm: AlarmEntity)

    @Query("DELETE FROM alarm_table")
    suspend fun deleteAll()

    @Update
    suspend fun update(alarm: AlarmEntity)
}
