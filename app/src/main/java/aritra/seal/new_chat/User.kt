package aritra.seal.new_chat

data class User(
    val email: String? = null,
    val imageUri: String? = null,
    val uid: String? = null,
    var username: String? = null
)
{
constructor() : this(null.toString(), null, null.toString(), null.toString())
}