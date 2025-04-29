package pw.cdezselfhosted.cdeznetmessaging.api

import pw.cdezselfhosted.cdeznetmessaging.Message
import retrofit2.Call
import retrofit2.http.*

data class Message(
    val username: String,
    val chat_room: String,
    val message: String
)

data class MessageResponse(
    val status: String,
    val messages: List<String>? // Change to List<String>
)

interface ChatApi {
    @POST("/send_message")
    fun sendMessage(@Body message: Message): Call<MessageResponse>

    @GET("/get_history")
    fun getMessageHistory(@Query("chat_room") chatRoom: String): Call<MessageResponse>
}