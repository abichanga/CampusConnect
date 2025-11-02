package com.example.mobile_app.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,          // Primary key

    val title: String,
    val description: String,
    val faculty: String,
    val location: String,
    val date: String,
    val time: String
)
