package st.youknow.bots

import java.math.BigInteger
import java.security.MessageDigest

import akka.actor.{Actor, Props}
import com.bot4s.telegram.Implicits._
import com.bot4s.telegram.api.Polling
import com.bot4s.telegram.api.declarative.{Commands, InlineQueries}
import com.bot4s.telegram.clients.ScalajHttpClient
import com.bot4s.telegram.methods.{ParseMode, PinChatMessage, SendMessage}
import com.bot4s.telegram.models.{InlineQueryResultArticle, InputTextMessageContent}
import slogging.StrictLogging
import st.youknow.updater.RSSActor._
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
  private val youAreSoFast = "Ты настолько быстр, что я не успел получить RSS!"
  private val announceGroupId = "-1001134192058"
  private var maybePodcast: Option[String] = None
  private var podcasts = PodcastsPayload(Seq.empty[PodcastEntry], PodcastMeta())

  override def preStart: Unit = {
    super.preStart()
    run()
  }

  onCommand("get_last_podcast") { implicit msg =>
    replyMd(
      maybePodcast.getOrElse(youAreSoFast),
      replyMarkup = listenButton(podcasts.podcasts.head.link))
  }

  onInlineQuery { implicit iq =>
    val query = iq.query

    if (query.isEmpty)
      answerInlineQuery(Seq())
    else {
      val queryLower = query.toLowerCase
      val foundInBody = false
      val (extractedPodcasts, meta) = getPodcasts(podcasts)
      val matchedPodcasts = extractedPodcasts.filter(x => {
        x.title.toLowerCase.contains(queryLower) || x.summary.toLowerCase.contains(queryLower)
      }).map(x => {
        InlineQueryResultArticle(
          query + "@" + x.link,
          title = x.title,
          inputMessageContent = InputTextMessageContent(build(x), disableWebPagePreview = true, parseMode = ParseMode.Markdown),
          thumbUrl = meta.logoUrl,
          description = matchLocation(foundInBody),
          replyMarkup = listenButton(x.link)
        )
      })

      logger.info("matches quantity: " + matchedPodcasts.length + " for word " + query)
      answerInlineQuery(matchedPodcasts, cacheTime = 1)
    }
  }

  def getPodcasts(p: PodcastsPayload): (Seq[PodcastEntry], PodcastMeta) = p.podcasts -> p.meta
  def matchLocation(foundInBody: Boolean): Option[String] = if (!foundInBody) Some("Найдено в заголовке") else Some("Найдено в описании")

  override def receive: Receive = {
    case PodcastsPayload(rssFeed, meta) =>
      podcasts = PodcastsPayload(rssFeed, meta)
      maybePodcast = Some(build(rssFeed.head))

      var podcastHash = ""
      val h = hash(rssFeed.head.title)
      if (podcastHash.isEmpty) podcastHash = h
      if (podcastHash != h) request(SendMessage(announceGroupId, maybePodcast.getOrElse(youAreSoFast), ParseMode.Markdown)) foreach {f => request(PinChatMessage(f.chat.id, f.messageId)) }
      else podcastHash = h
  }

  def hash(s: String): String = new BigInteger(1, MessageDigest.getInstance("MD5").digest(s.getBytes())).toString(16)

  /* Int(n) extractor */
  object Int {
    def unapply(s: String): Option[Int] = Try(s.toInt).toOption
  }
}
