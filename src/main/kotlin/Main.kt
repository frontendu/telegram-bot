import com.github.kotlintelegrambot.bot
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.logging.HttpLoggingInterceptor
import services.soundcloud.SoundCloudWatcher
import services.telegram.Service

fun main() = runBlocking {
    val bot = bot {
        token =
            System.getenv("FU_TG_BOT_KEY") ?: throw IllegalStateException("env tg bot key FU_TG_BOT_KEY not provided")
        timeout = 30
        logLevel = HttpLoggingInterceptor.Level.NONE
    }

    val soundCloudRSS = "http://feeds.soundcloud.com/users/soundcloud:users:306631331/sounds.rss"
    val soundCloud = SoundCloudWatcher(soundCloudRSS)

    launch {
        Service(ch = soundCloud.ch, bot = bot).runPodcastSender()
    }

    soundCloud.watch()
    bot.startPolling()
}
