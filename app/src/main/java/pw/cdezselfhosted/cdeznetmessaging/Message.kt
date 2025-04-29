package pw.cdezselfhosted.cdeznetmessaging

data class MessageResponse(
    val status: String,
    val messages: List<String>? // Change from List<Message>? to List<String>?
)

data class Message(
    val username: String,
    val chatRoom: String,
    val message: String,
    val timestamp: String
)