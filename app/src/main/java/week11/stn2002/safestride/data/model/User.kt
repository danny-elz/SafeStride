package week11.stn2002.safestride.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val emergencyContact: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
