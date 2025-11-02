package com.example.mobile_app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Query
import androidx.room.OnConflictStrategy

@Dao
interface EventDao {

    // CREATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    // UPDATE
    @Update
    suspend fun updateEvent(event: EventEntity)

    // DELETE
    @Delete
    suspend fun deleteEvent(event: EventEntity)

    // READ
    @Query("SELECT * FROM events ORDER BY date, time")
    suspend fun getAllEvents(): List<EventEntity>
}
