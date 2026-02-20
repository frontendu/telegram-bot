package services.telegram

import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import services.soundcloud.Podcast

typealias FormattedPodcastMessage = String

fun FormattedPodcastMessage.markAsNew(): String {
    return "${"\uD83C\uDF89".repeat(3)} Новый выпуск! \uD83E\uDD42 \n\n $this"
}

object Message {
    private const val listenPodcastButton = "\ud83c\udfa7 Слушать подкаст \ud83c\udfa7"
    private val timeCodeRegexp = "\\(([0-9]+(:[0-9]?).(:[0-9]+)?)\\)".toRegex()

    fun podcastMessage(message: Podcast, withLink: Boolean = false): FormattedPodcastMessage {
        return """
<strong>${message.title}</strong>

${prettifyDescription(message.description, message.link)}

${if (withLink) "<a href='${message.link}'>$listenPodcastButton</a>" else ""}
""".trimIndent()
    }

    fun buildListenButton(URL: String) =
        InlineKeyboardMarkup.createSingleButton(
            InlineKeyboardButton.Url(text = listenPodcastButton, url = URL)
        )

    private fun prettifyDescription(description: String, URL: String): String {
        val split = description.split("\n\n")
        if (split.size < 2) {
            return ""
        }

        val (timeCodes, links) = split

        val t = timeCodes.split("\n").map {
            val rs = timeCodeRegexp.find(it)?.value
            if (rs != null) "<a href='$URL#t=${rs.replace("[(|)]".toRegex(), "")}'>${it.replaceFirst(rs, "")
                .trimStart()}</a>"
            else it
        }

        val l = links.split("\n").map {
            val s = it.split(" ")
            "<a href='${s.last()}'>${s.dropLast(1).joinToString(separator = " ")}</a>"
        }

        return t.joinToString(separator = "\n") + "\n\n" + l.joinToString(separator = "\n")
    }
}
