import com.github.kotlintelegrambot.bot
import okhttp3.logging.HttpLoggingInterceptor
import services.soundcloud.SoundCloudWatcher

fun main() {
    val bot = bot {
        token =
            System.getenv("FU_TG_BOT_KEY") ?: throw IllegalStateException("env tg bot key FU_TG_BOT_KEY not provided")
        timeout = 30
        logLevel = HttpLoggingInterceptor.Level.NONE
    }

    val soundCloudRSS = "http://feeds.soundcloud.com/users/soundcloud:users:306631331/sounds.rss"

    SoundCloudWatcher(soundCloudRSS, bot).watch()
    bot.startPolling()
}
