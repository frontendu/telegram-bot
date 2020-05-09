package services.telegram

import com.github.kotlintelegrambot.entities.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import services.soundcloud.Podcast

object Telegram {
    private const val listenPodcastButton = "\ud83c\udfa7 Слушать подкаст \ud83c\udfa7"
    private var trombones = "${"\uD83C\uDF89".repeat(3)} Новый выпуск! 🥂"
    private val timeCodeRegexp = "^([0-9]+(:[0-9]?).(:[0-9]+)?)".toRegex()

    fun buildMessage(message: Podcast): String {
        return """
$trombones

<strong>${message.title}</strong>

${prettifyDescription(message.description, message.link)}
""".trimIndent()
    }

    fun buildListenButton(URL: String) =
        InlineKeyboardMarkup.createSingleButton(InlineKeyboardButton(text = listenPodcastButton, url = URL))

    private fun prettifyDescription(description: String, URL: String): String {
        return description.split("\n").map {
            val rs = timeCodeRegexp.find(it)?.value
            if (rs != null) "<a href='$URL#t=${rs}'>${it.replaceFirst(rs, "").trimStart()}</a>"
            else it
        }.joinToString("\n")
    }
}
