package pro.devlike.waha

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.charsets.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.*

@Serializable
data class ChadBody(
    val session: String,
    val chatId: String,
)

@Serializable
data class MessageBody(
    val session: String,
    val chatId: String,
    val text: String,
)

@Serializable
data class SendSeenBody(
    val session: String,
    val chatId: String,
    val messageId: String,
)


class WAHA(val host: String, val session: String = "default") {
    private val client = HttpClient {
        expectSuccess = true
        install(ContentNegotiation) {
            json()
        }
    }

    private suspend inline fun <reified T> post(uri: String, body: T): String {
        val response: HttpResponse = this.client.post("${host}${uri}") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        return response.bodyAsText()
    }

    suspend fun sendText(chatId: String, text: String): String {
        return post("/api/sendText", MessageBody(session = session, chatId = chatId, text = text))
    }

    suspend fun sendSeen(chatId: String, messageId: String): String {
        return post("/api/sendSeen", SendSeenBody(session = session, chatId = chatId, messageId = messageId))
    }

    suspend fun startTyping(chatId: String): String {
        return post("/api/startTyping", ChadBody(session = session, chatId = chatId))
    }

    suspend fun stopTyping(chatId: String): String {
        return post("/api/stopTyping", ChadBody(session = session, chatId = chatId))
    }

    suspend fun typing(chatId: String, duration: Int) {
        startTyping(chatId)
        delay(duration * 1000L)
        stopTyping(chatId)
    }
}
