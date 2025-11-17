package Models

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val faculty: String = "",
    val role: String = "student"   // default for all signups
)
