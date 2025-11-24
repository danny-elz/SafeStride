package week11.stn2002.safestride.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import week11.stn2002.safestride.data.model.SOSAlert
import week11.stn2002.safestride.util.Resource

class AlertRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun createAlert(alert: SOSAlert): Resource<String> {
        return try {
            val docRef = firestore.collection("alerts")
                .add(alert)
                .await()
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create alert")
        }
    }

    suspend fun getUserAlerts(userId: String): Resource<List<SOSAlert>> {
        return try {
            val snapshot = firestore.collection("alerts")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val alerts = snapshot.documents.mapNotNull { doc ->
                doc.toObject(SOSAlert::class.java)?.copy(id = doc.id)
            }
            // Sort in app instead of using Firestore orderBy (which requires an index)
            val sortedAlerts = alerts.sortedByDescending { it.timestamp }
            Resource.Success(sortedAlerts)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load alerts")
        }
    }

    fun getUserAlertsFlow(userId: String): Flow<Resource<List<SOSAlert>>> = callbackFlow {
        val listener = firestore.collection("alerts")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // If index error, return empty list instead of error
                    if (error.message?.contains("index") == true ||
                        error.message?.contains("FAILED_PRECONDITION") == true) {
                        trySend(Resource.Success(emptyList()))
                    } else {
                        trySend(Resource.Error(error.message ?: "Failed to load alerts"))
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val alerts = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(SOSAlert::class.java)?.copy(id = doc.id)
                    }
                    // Sort in app since Firestore index might not be ready
                    val sortedAlerts = alerts.sortedByDescending { it.timestamp }
                    trySend(Resource.Success(sortedAlerts))
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun deleteAlert(alertId: String): Resource<Unit> {
        return try {
            firestore.collection("alerts")
                .document(alertId)
                .delete()
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete alert")
        }
    }

    suspend fun resolveAlert(alertId: String): Resource<Unit> {
        return try {
            firestore.collection("alerts")
                .document(alertId)
                .update("resolved", true)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to resolve alert")
        }
    }
}
