import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.inlineQuery
import com.github.kotlintelegrambot.dispatcher.message
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.logging.HttpLoggingInterceptor
import services.soundcloud.PodcastMessage
import services.soundcloud.SoundCloudWatcher
import services.telegram.MessageSendResponse
import services.telegram.Service

@ExperimentalCoroutinesApi
fun main() = runBlocking {
    val soundCloudRSS = "http://feeds.soundcloud.com/users/soundcloud:users:306631331/sounds.rss"
    val statusCh = Channel<MessageSendResponse>()
    val messageCh = Channel<PodcastMessage>(1)

    val soundCloudWatcher = SoundCloudWatcher(soundCloudRSS, messageCh, statusCh)

    val bot = bot {
        token =
            System.getenv("FU_TG_BOT_KEY")
                ?: throw IllegalStateException("env tg bot key FU_TG_BOT_KEY not provided")
        timeout = 5
        logLevel = HttpLoggingInterceptor.Level.NONE
        dispatch {
            inlineQuery { bot, iq ->
                handleInlineQuery(bot, iq, soundCloudWatcher.allPodcastsTitles)
            }
        }
    }

    launch {
        Service(messageCh, statusCh, bot).runPodcastSender()
    }

    soundCloudWatcher.watch()
    bot.startPolling()
}
