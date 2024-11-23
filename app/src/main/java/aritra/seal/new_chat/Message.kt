package aritra.seal.new_chat

data class Message(
    var senderId: String = "",
    var receiverId: String = "",
    var text: String = "",
    var timestamp: Long = 0,
    var seen: Boolean = false
)

