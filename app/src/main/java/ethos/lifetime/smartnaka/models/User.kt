package ethos.lifetime.smartnaka.models

data class User(
    val uid: String = "",
    var name: String = "",
    val email: String = "",
    var photoUrl: String = "",
    var logs: MutableList<VehicleLog> = mutableListOf()
)