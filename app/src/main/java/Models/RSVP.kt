package Models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class RSVP(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val eventId: String = "",
    @ServerTimestamp
    val rsvpDate: Date? = null,
    val status: RSVPStatus = RSVPStatus.CONFIRMED
)

enum class RSVPStatus {
    CONFIRMED,
    CANCELLED
}