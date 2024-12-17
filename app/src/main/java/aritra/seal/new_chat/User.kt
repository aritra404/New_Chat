package aritra.seal.new_chat

data class User(
    val email: String,
    val imageUri: String?,
    val uid: String,
    val username: String,
    val publicKey: String
)

{
constructor() : this(null.toString(), null, null.toString(), null.toString(),null.toString())
}