package services.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import services.soundcloud.PodcastMessage

data class MessageSendResponse(val ok: Boolean, val message: String)

class Service(
    private val ch: ReceiveChannel<PodcastMessage>,
    private val statusCh: Channel<MessageSendResponse>,
    private val bot: Bot
) {
    companion object {
        val PARSE_MODE = ParseMode.HTML
    }

    private val logger = KotlinLogging.logger {}
    private val chatID = -1001312727708

    fun runPodcastSender() = runBlocking {
        while (true) {
            val podcast = ch.receive()
            val (result, exception) = bot.sendMessage(
                chatId = chatID,
                text = Message.podcastMessage(podcast.body).markAsNew(),
                parseMode = PARSE_MODE,
                replyMarkup = Message.buildListenButton(podcast.body.link)
            )

            if (exception != null) {
                logger.error { "cannot send podcast message: $exception.message" }
                statusCh.send(MessageSendResponse(false, exception.message ?: "empty error from telegram api"))
                return@runBlocking
            }

            val messageID = result?.body()?.result?.messageId
            if (messageID == null) {
                logger.error { "cannot get message id: result is null" }
                statusCh.send(MessageSendResponse(false, "null result from telegram api"))
                return@runBlocking
            }

            bot.pinChatMessage(chatId = chatID, messageId = messageID)
            statusCh.send(MessageSendResponse(true, "sent successfully"))
        }
    }
}
