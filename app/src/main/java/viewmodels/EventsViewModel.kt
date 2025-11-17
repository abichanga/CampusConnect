package viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import Models.Event
import repositories.EventRepository
import repositories.RSVPRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import utils.Constants.HARDCODED_USER_ID

// These classes are part of the same file
sealed class EventsUiState {
    object Loading : EventsUiState()
    data class Success(val events: List<EventWithStatus>) : EventsUiState()
    data class Error(val message: String) : EventsUiState()
}

data class EventWithStatus(
    val event: Event,
    val hasUserRSVPd: Boolean = false,
    val isProcessing: Boolean = false
)

class EventsViewModel : ViewModel() {
    private val eventRepository = EventRepository()
    private val rsvpRepository = RSVPRepository()
    //private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    private val _rsvpSuccess = MutableStateFlow<String?>(null)
    val rsvpSuccess: StateFlow<String?> = _rsvpSuccess.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = EventsUiState.Loading

            val result = eventRepository.getAllEvents()
            result.onSuccess { events ->
                val eventsWithStatus = events.map { event ->
                    //val userId = auth.currentUser?.uid ?: ""
                    val userId = HARDCODED_USER_ID
                    val hasRSVPd = rsvpRepository.hasUserRSVPd(userId, event.eventID)
                        .getOrDefault(false)

                    EventWithStatus(event, hasRSVPd)
                }
                _uiState.value = EventsUiState.Success(eventsWithStatus)
            }.onFailure { exception ->
                _uiState.value = EventsUiState.Error(
                    exception.message ?: "Failed to load events"
                )
            }
        }
    }

    fun rsvpToEvent(event: Event) {
        //val userId = auth.currentUser?.uid ?: return
        val userId = HARDCODED_USER_ID


        viewModelScope.launch {
            updateEventProcessingState(event.eventID, true)

            if (event.isFull()) {
                _rsvpSuccess.value = "Event is full"
                updateEventProcessingState(event.eventID, false)
                return@launch
            }

            val rsvpResult = rsvpRepository.createRSVP(userId, event.eventID)
            rsvpResult.onSuccess {
                eventRepository.incrementAttendees(event.eventID)
                updateEventRSVPStatus(event.eventID, true)
                _rsvpSuccess.value = "RSVP successful!"
            }.onFailure { exception ->
                _rsvpSuccess.value = exception.message ?: "RSVP failed"
            }

            updateEventProcessingState(event.eventID, false)
        }
    }

    fun cancelRSVP(event: Event) {
//        val userId = auth.currentUser?.uid ?: return
        val userId = HARDCODED_USER_ID


        viewModelScope.launch {
            updateEventProcessingState(event.eventID, true)

            val result = rsvpRepository.cancelRSVP(userId, event.eventID)
            result.onSuccess {
                eventRepository.decrementAttendees(event.eventID)
                updateEventRSVPStatus(event.eventID, false)
                _rsvpSuccess.value = "RSVP cancelled"
            }.onFailure { exception ->
                _rsvpSuccess.value = exception.message ?: "Cancel failed"
            }

            updateEventProcessingState(event.eventID, false)
        }
    }

    private fun updateEventRSVPStatus(eventId: String, hasRSVPd: Boolean) {
        val currentState = _uiState.value
        if (currentState is EventsUiState.Success) {
            val updatedEvents = currentState.events.map { eventWithStatus ->
                if (eventWithStatus.event.eventID == eventId) {
                    eventWithStatus.copy(hasUserRSVPd = hasRSVPd)
                } else {
                    eventWithStatus
                }
            }
            _uiState.value = EventsUiState.Success(updatedEvents)
        }
    }

    private fun updateEventProcessingState(eventId: String, isProcessing: Boolean) {
        val currentState = _uiState.value
        if (currentState is EventsUiState.Success) {
            val updatedEvents = currentState.events.map { eventWithStatus ->
                if (eventWithStatus.event.eventID == eventId) {
                    eventWithStatus.copy(isProcessing = isProcessing)
                } else {
                    eventWithStatus
                }
            }
            _uiState.value = EventsUiState.Success(updatedEvents)
        }
    }

    fun clearRSVPMessage() {
        _rsvpSuccess.value = null
    }
}