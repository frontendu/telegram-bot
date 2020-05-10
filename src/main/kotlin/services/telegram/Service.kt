package services.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import services.soundcloud.NewPodcastMessage

class Service(val ch: Channel<NewPodcastMessage>, val bot: Bot) {
    private val logger = KotlinLogging.logger {}
    private val chatID = -1001312727708


    fun runPodcastSender() = runBlocking {
        while (true) {
            val message = ch.receive()

            val (result, exception) = bot.sendMessage(
                chatId = chatID,
                text = Message.buildMessage(message.body),
                parseMode = ParseMode.HTML,
                replyMarkup = Message.buildListenButton(message.body.link)
            )

            if (exception != null) {
                logger.error { "cannot send podcast message: $exception.message" }
                return@runBlocking
            }

            val messageID = result?.body()?.result?.messageId
                ?: run {
                    logger.error { "cannot get message id: result is null" }
                    return@runBlocking
                }

            bot.pinChatMessage(chatId = chatID, messageId = messageID)
        }
    }
}
