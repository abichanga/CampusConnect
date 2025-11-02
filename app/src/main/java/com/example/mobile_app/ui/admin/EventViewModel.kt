package com.example.mobile_app.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mobile_app.data.AppDatabase
import com.example.mobile_app.data.EventEntity
import com.example.mobile_app.data.EventRepository
import kotlinx.coroutines.launch


class EventViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EventRepository

    // Live data list of events for the UI to observe
    private val _events = MutableLiveData<List<EventEntity>>()
    val events: LiveData<List<EventEntity>> get() = _events

    init {
        // Build database, dao, repository chain
        val db = AppDatabase.getDatabase(application)
        repository = EventRepository(db.eventDao())

        // Load events immediately when ViewModel is created
        loadEvents()
    }

    // Read all events from DB
    fun loadEvents() {
        viewModelScope.launch {
            val result = repository.getEvents()
            _events.postValue(result)
        }
    }

    // Create new event
    fun addEvent(
        title: String,
        description: String,
        faculty: String,
        location: String,
        date: String,
        time: String
    ) {
        viewModelScope.launch {
            val event = EventEntity(
                title = title,
                description = description,
                faculty = faculty,
                location = location,
                date = date,
                time = time
            )
            repository.addEvent(event)
            loadEvents() // refresh list
        }
    }

    // Update an existing event
    fun updateEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.updateEvent(event)
            loadEvents()
        }
    }

    // Delete an existing event
    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.deleteEvent(event)
            loadEvents()
        }
    }
}
