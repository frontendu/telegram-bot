import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.InlineQuery
import com.github.kotlintelegrambot.entities.inlinequeryresults.InlineQueryResult
import com.github.kotlintelegrambot.entities.inlinequeryresults.InputMessageContent
import services.soundcloud.AllPodcasts
import services.telegram.Message
import services.telegram.Service

fun handleInlineQuery(bot: Bot, iq: InlineQuery, podcastsTitles: AllPodcasts?) {
    val queryText = iq.query.toLowerCase()
    if (queryText.isBlank() || queryText.isEmpty() || podcastsTitles == null) return

    val list = podcastsTitles.channel.item
        .filter { it.title.contains(queryText, ignoreCase = true) }
        .map { podcast ->
            InlineQueryResult.Article(
                id = podcast.title,
                title = podcast.title,
                inputMessageContent = InputMessageContent.Text(
                    Message.podcastMessage(podcast, true),
                    parseMode = Service.PARSE_MODE
                ),
                description = "Найдено"
            )
        }

    bot.answerInlineQuery(iq.id, list)
}
