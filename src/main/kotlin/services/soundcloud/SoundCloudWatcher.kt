package services.soundcloud

import com.github.kotlintelegrambot.entities.ParseMode
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import services.xml.Parser
import java.io.File
import java.io.FileNotFoundException
import kotlin.concurrent.fixedRateTimer

data class NewPodcastMessage(
    val body: Podcast,
    val parseMode: ParseMode
)

class SoundCloudWatcher(private val soundCloudRSS: String) {
    private val logger = KotlinLogging.logger {}
    private val httpClient = HttpClient()
    private val scheduleRSSIntervalMs = 1000 * 60L
    private val lastPodcastFilename = "last_podcast_number.txt"
    private val lastPodcastFile = File(lastPodcastFilename)

    val ch = Channel<NewPodcastMessage>()

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
                        ch.send(NewPodcastMessage(body = lastPodcast, parseMode = ParseMode.HTML))
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
