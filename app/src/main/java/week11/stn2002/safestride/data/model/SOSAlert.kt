package week11.stn2002.safestride.data.model

data class SOSAlert(
    val id: String = "",
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isAutomatic: Boolean = false,
    val resolved: Boolean = false,
    val address: String = ""
)
