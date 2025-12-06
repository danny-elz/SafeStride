package week11.stn2002.safestride.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
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
            Log.d("AlertRepository", "Creating alert: isAutomatic=${alert.isAutomatic}, lat=${alert.latitude}, lng=${alert.longitude}")
            val docRef = firestore.collection("alerts")
                .add(alert)
                .await()
            Log.d("AlertRepository", "Alert saved to Firestore: ${docRef.id}")
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Log.e("AlertRepository", "Failed to create alert: ${e.message}")
            Resource.Error(e.message ?: "Failed to create alert")
        }
    }

    fun getUserAlertsFlow(userId: String): Flow<Resource<List<SOSAlert>>> = callbackFlow {
        Log.d("AlertRepository", "Setting up alerts listener for userId: $userId")

        val listener = firestore.collection("alerts")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AlertRepository", "Firestore error: ${error.message}", error)
                    // If index error or permission error, return empty list
                    if (error.message?.contains("index") == true ||
                        error.message?.contains("FAILED_PRECONDITION") == true ||
                        error.message?.contains("PERMISSION_DENIED") == true) {
                        Log.w("AlertRepository", "Permission or index issue, returning empty list")
                        trySend(Resource.Success(emptyList()))
                    } else {
                        trySend(Resource.Error(error.message ?: "Failed to load alerts"))
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d("AlertRepository", "Received ${snapshot.documents.size} alerts")
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
