package st.youknow.bots

import akka.actor.{Actor, Props}
import com.bot4s.telegram.Implicits._
import com.bot4s.telegram.api.Polling
import com.bot4s.telegram.api.declarative.{Commands, InlineQueries}
import com.bot4s.telegram.clients.ScalajHttpClient
import com.bot4s.telegram.methods.ParseMode
import com.bot4s.telegram.models.{InlineQueryResultArticle, InputTextMessageContent}
import slogging.StrictLogging
import st.youknow.updater.RSSActor.{PodcastEntry, PodcastMeta, Podcasts, TGResponse}
import st.youknow.{Builder, Markup, Parser}

import scala.util.Try

object Podcast {
  def apply(token: Option[String]): Props = {
    if (token.isEmpty) throw TelegramKeyIsNotProvided("Telegram key not found. Set it by FRONTENDU_TG_KEY env variable")
    Props(classOf[Podcast], token.get)
  }
}

class Podcast(override val token: String) extends AbstractBot(token: String) with Parser with Builder with Markup with Actor
  with Polling
  with Commands with InlineQueries
  with StrictLogging {
  override val client = new ScalajHttpClient(token)
  private var maybePodcast: Option[String] = None
  private var podcasts = Seq.empty[PodcastEntry]
  private var podcastMeta = PodcastMeta("")

  override def preStart: Unit = {
    super.preStart()
    run()
  }

  onCommand("get_last_podcast") { implicit msg =>
    replyMd(
      maybePodcast.getOrElse("Ты настолько быстр, что я не успел получить RSS!"),
      replyMarkup = listenButton(podcasts.head.link))
  }

  onInlineQuery { implicit iq =>
    val query = iq.query

    if (query.isEmpty)
      answerInlineQuery(Seq())
    else {
      val queryLower = query.toLowerCase
      val foundInBody = false
      val matchedPodcasts = podcasts.filter(x => {
        x.title.toLowerCase.contains(queryLower) || x.summary.toLowerCase.contains(queryLower)
      }).map(x => {
        InlineQueryResultArticle(
          query + "@" + x.link,
          title = x.title,
          inputMessageContent = InputTextMessageContent(build(x), disableWebPagePreview = true, parseMode = ParseMode.Markdown),
          thumbUrl = podcastMeta.logoUrl,
          description = matchLocation(foundInBody),
          replyMarkup = listenButton(x.link)
        )
      })

      answerInlineQuery(matchedPodcasts, cacheTime = 1)
    }
  }

  def matchLocation(foundInBody: Boolean): Option[String] = if (!foundInBody) Some("Найдено в заголовке") else Some("Найдено в описании")

  override def receive: Receive = {
    case TGResponse(text) =>
      logger.info("received template from rss actor")
      maybePodcast = Some(text)
    case Podcasts(rssFeed, meta) =>
      logger.info("received full rss feed")
      podcasts = rssFeed
      podcastMeta = meta
  }

  /* Int(n) extractor */
  object Int {
    def unapply(s: String): Option[Int] = Try(s.toInt).toOption
  }
}
