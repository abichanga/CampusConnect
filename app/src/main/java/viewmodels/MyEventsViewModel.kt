package viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import Models.EventWithRSVP
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import repositories.EventRepository
import repositories.RSVPRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import utils.Constants.HARDCODED_USER_ID

sealed class MyEventsUiState {
    object Loading : MyEventsUiState()
    data class Success(
        val upcomingEvents: List<EventWithRSVP>,
        val pastEvents: List<EventWithRSVP>
    ) : MyEventsUiState()
    data class Error(val message: String) : MyEventsUiState()
    object Empty : MyEventsUiState()
}

class MyEventsViewModel : ViewModel() {
    private val eventRepository = EventRepository()
    private val rsvpRepository = RSVPRepository()

    private val _uiState = MutableStateFlow<MyEventsUiState>(MyEventsUiState.Loading)
    val uiState: StateFlow<MyEventsUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "MyEventsViewModel"
    }

    init {
        loadMyEvents()
    }

    fun loadMyEvents() {
        val userId = HARDCODED_USER_ID
        Log.d(TAG, "Loading events for user: $userId")

        viewModelScope.launch {
            _uiState.value = MyEventsUiState.Loading

            val rsvpsResult = rsvpRepository.getUserRSVPs(userId)
            rsvpsResult.onSuccess { rsvps ->
                Log.d(TAG, "Found ${rsvps.size} RSVPs")

                if (rsvps.isEmpty()) {
                    Log.d(TAG, "No RSVPs found - showing empty state")
                    _uiState.value = MyEventsUiState.Empty
                    return@onSuccess
                }

                // Log each RSVP for debugging
                rsvps.forEachIndexed { index, rsvp ->
                    Log.d(TAG, "RSVP $index: eventId=${rsvp.eventId}, userId=${rsvp.userId}, status=${rsvp.status}")
                }

                // Fetch all events asynchronously
                val eventsWithRSVP = rsvps.map { rsvp ->
                    viewModelScope.async {
                        Log.d(TAG, "Fetching event: ${rsvp.eventId}")
                        val eventResult = eventRepository.getEventById(rsvp.eventId)
                        eventResult.onSuccess { event ->
                            Log.d(TAG, "Successfully fetched event: ${event?.eventID} - ${event?.title}")
                        }.onFailure { error ->
                            Log.e(TAG, "Failed to fetch event ${rsvp.eventId}: ${error.message}")
                        }

                        eventResult.getOrNull()?.let { event ->
                            EventWithRSVP(event, rsvp)
                        }
                    }
                }.awaitAll().filterNotNull()

                Log.d(TAG, "Fetched ${eventsWithRSVP.size} events successfully")

                if (eventsWithRSVP.isEmpty()) {
                    Log.d(TAG, "No events found after fetching - showing empty state")
                    _uiState.value = MyEventsUiState.Empty
                    return@onSuccess
                }

                // Categorize events
                val currentTime = System.currentTimeMillis()
                Log.d(TAG, "Current time: $currentTime")

                val upcoming = eventsWithRSVP.filter { eventWithRSVP ->
                    val eventTime = eventWithRSVP.event.datetime?.toDate()?.time ?: Long.MAX_VALUE
                    val isUpcoming = eventTime >= currentTime
                    Log.d(TAG, "Event ${eventWithRSVP.event.title}: time=$eventTime, isUpcoming=$isUpcoming")
                    isUpcoming
                }.sortedBy { it.event.datetime?.toDate()?.time }

                val past = eventsWithRSVP.filter { eventWithRSVP ->
                    val eventTime = eventWithRSVP.event.datetime?.toDate()?.time ?: 0
                    eventTime < currentTime
                }.sortedByDescending { it.event.datetime?.toDate()?.time }

                Log.d(TAG, "Upcoming events: ${upcoming.size}, Past events: ${past.size}")

                _uiState.value = MyEventsUiState.Success(upcoming, past)
            }.onFailure { exception ->
                Log.e(TAG, "Failed to load RSVPs: ${exception.message}", exception)
                _uiState.value = MyEventsUiState.Error(
                    exception.message ?: "Failed to load events"
                )
            }
        }
    }

    fun cancelRSVP(eventId: String) {
        val userId = HARDCODED_USER_ID

        viewModelScope.launch {
            val result = rsvpRepository.cancelRSVP(userId, eventId)
            result.onSuccess {
                eventRepository.decrementAttendees(eventId)
                loadMyEvents()
            }
        }
    }
}