package repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import Models.Event
import android.util.Log
import kotlinx.coroutines.tasks.await

class EventRepository {
    private val db = FirebaseFirestore.getInstance()
    private val eventsCollection = db.collection("events")

    // Get all events ordered by date
    suspend fun getAllEvents(): Result<List<Event>> {
        return try {
            val snapshot = eventsCollection
                .orderBy("datetime", Query.Direction.ASCENDING)
                .get()
                .await()

            val events = snapshot.toObjects(Event::class.java)
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get upcoming events only
    suspend fun getUpcomingEvents(): Result<List<Event>> {
        return try {
            val currentTime = System.currentTimeMillis()
            val snapshot = eventsCollection
                .whereGreaterThan("datetime", currentTime)
                .orderBy("datetime", Query.Direction.ASCENDING)
                .get()
                .await()

            val events = snapshot.toObjects(Event::class.java)
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get event by ID
    suspend fun getEventById(eventId: String): Result<Event?> {
        return try {
            // Query by eventID field, not document ID
            val snapshot = eventsCollection
                .whereEqualTo("eventID", eventId)
                .limit(1)
                .get()
                .await()

            val event = if (!snapshot.isEmpty) {
                snapshot.documents[0].toObject(Event::class.java)
            } else {
                null
            }

            Log.d("EventRepo", "Found event: ${event?.title}")
            Result.success(event)
        } catch (e: Exception) {
            Log.e("EventRepo", "Error: ${e.message}")
            Result.failure(e)
        }
    }

    // Update attendee count when someone RSVPs
    suspend fun incrementAttendees(eventId: String): Result<Unit> {
        return try {
            // Find document by eventID field
            val snapshot = eventsCollection
                .whereEqualTo("eventID", eventId)
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                snapshot.documents[0].reference
                    .update("currentAttendees", com.google.firebase.firestore.FieldValue.increment(1))
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun decrementAttendees(eventId: String): Result<Unit> {
        return try {
            // Find document by eventID field
            val snapshot = eventsCollection
                .whereEqualTo("eventID", eventId)
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                snapshot.documents[0].reference
                    .update("currentAttendees", com.google.firebase.firestore.FieldValue.increment(-1))
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}