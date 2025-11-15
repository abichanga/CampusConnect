package Models

data class EventWithRSVP(
    val event: Event,
    val rsvp: RSVP,
    val hasUserRSVPd: Boolean = true
)