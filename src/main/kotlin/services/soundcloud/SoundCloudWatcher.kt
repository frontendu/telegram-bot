package services.soundcloud

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.*
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import services.telegram.Telegram
import services.xml.Parser
import java.io.File
import java.io.FileNotFoundException
import kotlin.concurrent.fixedRateTimer

class SoundCloudWatcher(private val soundCloudRSS: String, private val bot: Bot) {
    private val chatID = -1001312727708
    private val logger = KotlinLogging.logger {}
    private val httpClient = HttpClient()

    private val scheduleRSSIntervalMs = 1000 * 60L
    private val lastPodcastFilename = "last_podcast_number.txt"
    private val lastPodcastFile = File(lastPodcastFilename)

    fun watch() {
        fixedRateTimer("rss updater", true, 0, scheduleRSSIntervalMs) {
            runBlocking {
                val t1 = System.currentTimeMillis()
                logger.info { "start update rss $soundCloudRSS" }
                val lastPodcast = getLastPodcast()
                logger.info { "finish update rss $soundCloudRSS for ${System.currentTimeMillis() - t1} ms" }

                val currentPodcastNumber = readLastPodcastNumber()

                lastPodcast?.run {
                    if (lastPodcast.getPodcastNumber() > currentPodcastNumber) {
                        val (result, exception) = bot.sendMessage(
                            chatId = chatID,
                            text = Telegram.buildMessage(lastPodcast),
                            parseMode = ParseMode.HTML,
                            replyMarkup = Telegram.buildListenButton(lastPodcast.link)
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
                        lastPodcastFile.writeText(lastPodcast.getPodcastNumber().toString())
                    }
                } ?: logger.error { "soundcloud rss is empty" }
            }
        }
    }

    private suspend fun getLastPodcast(): Podcast? {
        return try {
            Parser.parseAs<Message>(httpClient.get(soundCloudRSS)).getLastPodcast()
        } catch (e: Exception) {
            logger.error { "cannot get soundcloud RSS ${e.message}" }
            null
        }
    }

    private fun readLastPodcastNumber(): Int {
        return try {
            lastPodcastFile.readText().toInt()
        } catch (e: Exception) {
            when (e) {
                is FileNotFoundException -> logger.error { "file $lastPodcastFilename not found" }
                else -> logger.error { "cannot read last podcast number $e.message" }
            }
            0
        }
    }
}
