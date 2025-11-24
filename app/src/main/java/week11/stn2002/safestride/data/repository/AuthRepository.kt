package week11.stn2002.safestride.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import week11.stn2002.safestride.data.model.User
import week11.stn2002.safestride.util.Resource

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun register(email
                         : String, password: String, emergencyContact: String): Resource<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                // Create user profile in Firestore
                val userProfile = User(
                    uid = user.uid,
                    email = email,
                    emergencyContact = emergencyContact
                )
                firestore.collection("users")
                    .document(user.uid)
                    .set(userProfile)
                    .await()
                Resource.Success(user)
            } else {
                Resource.Error("Registration failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred during registration")
        }
    }

    suspend fun login(email: String, password: String): Resource<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Resource.Success(user)
            } else {
                Resource.Error("Login failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred during login")
        }
    }

    suspend fun resetPassword(email: String): Resource<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send password reset email")
        }
    }

    suspend fun getUserProfile(uid: String): Resource<User> {
        return try {
            val document = firestore.collection("users")
                .document(uid)
                .get()
                .await()
            val user = document.toObject(User::class.java)
            if (user != null) {
                Resource.Success(user)
            } else {
                Resource.Error("User profile not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load user profile")
        }
    }

    fun logout() {
        auth.signOut()
    }
}
