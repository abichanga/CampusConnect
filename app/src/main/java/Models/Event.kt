package Models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Event(
    //@DocumentId
    val eventID: String = "",
    val title: String = "",
    val description: String = "",
    val venue: String = "",
    val datetime: Timestamp? = null, // Timestamp in milliseconds
    val faculty: String = "",
    val maxAttendees: Int = 0,
    val currentAttendees: Int = 0,
    val posterIDUrl: String = "",
    val organizerID: String = "",
    @ServerTimestamp
    val createdAt: Date? = null
) {
    // Helper function to check if event is full
    fun isFull(): Boolean = currentAttendees >= maxAttendees

    // Helper function to check if event is upcoming
    fun isUpcoming(): Boolean = datetime?.toDate()?.time?.let { it > System.currentTimeMillis() } ?: false

    // Helper function to get available spots
    fun getAvailableSpots(): Int = maxAttendees - currentAttendees
}