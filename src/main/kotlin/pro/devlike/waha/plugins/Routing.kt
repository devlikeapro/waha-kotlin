package pro.devlike.waha.plugins

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import pro.devlike.waha.WAHA
import java.util.*

@Serializable
data class MessagePayload(
    val body: String?, // Text
    val from: String, // Number in format 1231231231@c.us or @g.us for group
    val id: String, // Message ID - false_11111111111@c.us_AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA

)

@Serializable
data class WebhookBody(
    val event: String, // message, message.ack, etc
    val payload: MessagePayload
)

fun Application.configureRouting() {
    val waha = WAHA("http://localhost:3000")

    routing {
        get("/") {
            call.respondText("WhatsApp Echo Bot is ready!")
        }
        get("/bot") {
            call.respondText("WhatsApp Echo Bot is ready!")
        }
        post("/bot") {
            val body = call.receive<WebhookBody>()
            if (body.event != "message") {
                //  We can't process other event yet
                call.respondText("Unknown event: '${body.event}'")
                return@post
            }

            val payload = body.payload
            val text = payload.body
            if (text == null || text == "") {
                // We can't process non-text messages yet
                call.respondText("OK")
                return@post
            }

            // Number in format 1231231231@c.us or @g.us for group
            val chatId = payload.from
            // Message ID - false_11111111111@c.us_AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
            val messageId = payload.id

            // IMPORTANT - Always send seen before sending new message
            waha.sendSeen(chatId, messageId)
            // Typing...
            waha.typing(chatId, (3..6).random())
            // Send a text back via WhatsApp HTTP API
            waha.sendText(chatId, text = text)
            call.respondText("OK")
        }
    }
}
