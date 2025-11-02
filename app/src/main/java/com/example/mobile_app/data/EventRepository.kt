package com.example.mobile_app.data

// The Repository is the middle layer between ViewModel and DAO.
// UI should NEVER talk straight to DAO.
class EventRepository(private val eventDao: EventDao) {

    // CREATE
    suspend fun addEvent(event: EventEntity) {
        eventDao.insertEvent(event)
    }

    // READ
    suspend fun getEvents(): List<EventEntity> {
        return eventDao.getAllEvents()
    }

    // UPDATE
    suspend fun updateEvent(event: EventEntity) {
        eventDao.updateEvent(event)
    }

    // DELETE
    suspend fun deleteEvent(event: EventEntity) {
        eventDao.deleteEvent(event)
    }
}
