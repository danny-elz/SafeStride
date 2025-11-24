package week11.stn2002.safestride.data.model

data class LocationUpdate(
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val accuracy: Float = 0f
)
