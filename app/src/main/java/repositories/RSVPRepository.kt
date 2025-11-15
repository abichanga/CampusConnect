package repositories

import com.google.firebase.firestore.FirebaseFirestore
import Models.RSVP
import Models.RSVPStatus
import kotlinx.coroutines.tasks.await
import utils.Constants.HARDCODED_USER_ID

class RSVPRepository {
    private val db = FirebaseFirestore.getInstance()
    private val rsvpsCollection = db.collection("rsvps")

    // Create RSVP for user
    suspend fun createRSVP(userId: String, eventID: String): Result<RSVP> {
        return try {
            val rsvp = RSVP(
                userId = userId,
                //userId = userId,
                eventId = eventID,
                status = RSVPStatus.CONFIRMED
            )

            val docRef = rsvpsCollection.add(rsvp).await()
            val createdRSVP = rsvp.copy(id = docRef.id)
            Result.success(createdRSVP)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cancel RSVP
    suspend fun cancelRSVP(userId: String, eventId: String): Result<Unit> {
        return try {
            val snapshot = rsvpsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", RSVPStatus.CONFIRMED.name)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                snapshot.documents[0].reference
                    .update("status", RSVPStatus.CANCELLED.name)
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get all user's RSVPs
    suspend fun getUserRSVPs(userId: String): Result<List<RSVP>> {
        return try {
            val snapshot = rsvpsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", RSVPStatus.CONFIRMED.name)
                .get()
                .await()

            val rsvps = snapshot.toObjects(RSVP::class.java)
            Result.success(rsvps)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Check if user has RSVP'd to specific event
    suspend fun hasUserRSVPd(userId: String, eventId: String): Result<Boolean> {
        return try {
            val snapshot = rsvpsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", RSVPStatus.CONFIRMED.name)
                .get()
                .await()

            Result.success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get RSVP for specific event and user
    suspend fun getRSVP(userId: String, eventID: String): Result<RSVP?> {
        return try {
            val snapshot = rsvpsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("eventId", eventID)
                .whereEqualTo("status", RSVPStatus.CONFIRMED.name)
                .get()
                .await()

            val rsvp = if (!snapshot.isEmpty) {
                snapshot.toObjects(RSVP::class.java)[0]
            } else null

            Result.success(rsvp)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}