package services.soundcloud

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import services.telegram.MessageSendResponse
import services.xml.Parser
import kotlin.concurrent.fixedRateTimer

data class PodcastMessage(val body: Podcast)

@ExperimentalCoroutinesApi
class SoundCloudWatcher(
    private val soundCloudRSS: String,
    private val messageCh: Channel<PodcastMessage>,
    private val statusCh: ReceiveChannel<MessageSendResponse>
) {
    private val logger = KotlinLogging.logger {}
    private val httpClient = HttpClient()
    private val scheduleRSSIntervalMs = 1000 * 60L
    private var lastPodcastNumber = 0

    var allPodcastsTitles: AllPodcasts? = null

    fun watch() {
        fixedRateTimer("rss updater", true, 0, scheduleRSSIntervalMs) {
            runBlocking {
                val t1 = System.currentTimeMillis()
                logger.info { "start update rss $soundCloudRSS" }
                val podcasts = getLastPodcast()
                logger.info { "finish update rss $soundCloudRSS for ${System.currentTimeMillis() - t1} ms" }

                podcasts?.run {
                    val lastPodcast = PodcastMessage(body = podcasts.getLastPodcast())
                    allPodcastsTitles = podcasts

                    if (lastPodcast.getPodcastNumber() > lastPodcastNumber) {
                        messageCh.send(lastPodcast)
                        if (statusCh.receive().ok) {
                            lastPodcastNumber = lastPodcast.getPodcastNumber()
                        }
                    }
                } ?: logger.error { "soundcloud rss is empty" }
            }
        }
    }

    private suspend fun getLastPodcast(): AllPodcasts? {
        return try {
            Parser.parseAs<AllPodcasts>(httpClient.get(soundCloudRSS))
        } catch (e: Exception) {
            logger.error { "cannot get soundcloud RSS ${e.message}" }
            null
        }
    }
}
