package week11.stn2002.safestride.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import week11.stn2002.safestride.data.model.User
import week11.stn2002.safestride.util.Resource
import kotlin.coroutines.resume

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "AuthRepository"
        private const val TIMEOUT_MS = 15000L
    }

    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun register(email: String, password: String, emergencyContact: String): Resource<FirebaseUser> {
        return withContext(Dispatchers.Main) {
            try {
                Log.d(TAG, "Starting registration for: $email")
                var errorMessage: String? = null

                val result = withTimeoutOrNull(TIMEOUT_MS) {
                    suspendCancellableCoroutine { continuation ->
                        var resumed = false
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (resumed) return@addOnCompleteListener
                                resumed = true
                                if (task.isSuccessful) {
                                    Log.d(TAG, "Firebase auth success")
                                    continuation.resume(task.result)
                                } else {
                                    Log.e(TAG, "Firebase auth failed: ${task.exception?.message}")
                                    errorMessage = task.exception?.message
                                    continuation.resume(null)
                                }
                            }
                    }
                }

                if (result == null) {
                    Log.e(TAG, "Registration timed out or failed: $errorMessage")
                    return@withContext Resource.Error(errorMessage ?: "Registration failed. Please check your internet connection.")
                }

                val user = result.user
                if (user != null) {
                    Log.d(TAG, "User created with UID: ${user.uid}, saving profile to Firestore")
                    val userProfile = hashMapOf(
                        "uid" to user.uid,
                        "email" to email,
                        "emergencyContact" to emergencyContact,
                        "createdAt" to System.currentTimeMillis()
                    )

                    val firestoreSaved = withTimeoutOrNull(TIMEOUT_MS) {
                        suspendCancellableCoroutine { continuation ->
                            var resumed = false
                            firestore.collection("users")
                                .document(user.uid)
                                .set(userProfile)
                                .addOnCompleteListener { task ->
                                    if (resumed) return@addOnCompleteListener
                                    resumed = true
                                    if (task.isSuccessful) {
                                        Log.d(TAG, "Firestore profile saved successfully")
                                        continuation.resume(true)
                                    } else {
                                        Log.e(TAG, "Firestore save failed: ${task.exception?.message}")
                                        continuation.resume(false)
                                    }
                                }
                        }
                    }

                    if (firestoreSaved != true) {
                        Log.e(TAG, "Firestore save timed out or failed, but user was created")
                    }

                    Log.d(TAG, "Registration successful")
                    Resource.Success(user)
                } else {
                    Log.e(TAG, "Registration failed - user is null")
                    Resource.Error("Registration failed")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Registration error: ${e.message}", e)
                Resource.Error(e.message ?: "An error occurred during registration")
            }
        }
    }

    suspend fun login(email: String, password: String): Resource<FirebaseUser> {
        return withContext(Dispatchers.Main) {
            try {
                Log.d(TAG, "Starting login for: $email")
                var errorMessage: String? = null

                val result = withTimeoutOrNull(TIMEOUT_MS) {
                    suspendCancellableCoroutine { continuation ->
                        var resumed = false
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (resumed) return@addOnCompleteListener
                                resumed = true
                                if (task.isSuccessful) {
                                    Log.d(TAG, "Firebase login success")
                                    continuation.resume(task.result)
                                } else {
                                    Log.e(TAG, "Firebase login failed: ${task.exception?.message}")
                                    errorMessage = task.exception?.message
                                    continuation.resume(null)
                                }
                            }
                    }
                }

                if (result == null) {
                    Log.e(TAG, "Login timed out or failed: $errorMessage")
                    return@withContext Resource.Error(errorMessage ?: "Login timed out. Please check your internet connection.")
                }

                val user = result.user
                if (user != null) {
                    Log.d(TAG, "Login successful")
                    Resource.Success(user)
                } else {
                    Log.e(TAG, "Login failed - user is null")
                    Resource.Error("Login failed")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login error: ${e.message}", e)
                Resource.Error(e.message ?: "An error occurred during login")
            }
        }
    }

    suspend fun resetPassword(email: String): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Sending password reset email to: $email")
                val success = withTimeoutOrNull(TIMEOUT_MS) {
                    suspendCancellableCoroutine { continuation ->
                        auth.sendPasswordResetEmail(email)
                            .addOnSuccessListener {
                                Log.d(TAG, "Password reset email sent successfully")
                                continuation.resume(true)
                            }
                            .addOnFailureListener { exception ->
                                Log.e(TAG, "Password reset failed: ${exception.message}")
                                continuation.resume(false)
                            }
                    }
                }

                if (success == true) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error("Failed to send password reset email. Please check your internet connection.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Reset password error: ${e.message}", e)
                Resource.Error(e.message ?: "Failed to send password reset email")
            }
        }
    }

    suspend fun getUserProfile(uid: String): Resource<User> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Loading user profile for: $uid")
                val document = withTimeoutOrNull(TIMEOUT_MS) {
                    suspendCancellableCoroutine { continuation ->
                        firestore.collection("users")
                            .document(uid)
                            .get()
                            .addOnSuccessListener { doc ->
                                Log.d(TAG, "Firestore get success")
                                continuation.resume(doc)
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Firestore get failed: ${e.message}")
                                continuation.resume(null)
                            }
                    }
                }

                if (document == null) {
                    Log.e(TAG, "Get profile timed out or failed")
                    return@withContext Resource.Error("Request timed out. Please check your internet connection.")
                }

                val user = document.toObject(User::class.java)
                if (user != null) {
                    Log.d(TAG, "User profile loaded")
                    Resource.Success(user)
                } else {
                    Log.e(TAG, "User profile not found")
                    Resource.Error("User profile not found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Get profile error: ${e.message}", e)
                Resource.Error(e.message ?: "Failed to load user profile")
            }
        }
    }

    fun logout() {
        auth.signOut()
    }
}
