package st.youknow.updater

import akka.NotUsed
import akka.actor.{Actor, ActorRef, Props}
import com.softwaremill.sttp.{HttpURLConnectionBackend, Id, SttpBackend, UriInterpolator, sttp}
import slogging.StrictLogging
import st.youknow.{Builder, Parser, Markup}
import st.youknow.updater.RSSActor.{PodcastEntry, PodcastMeta, Podcasts, TGResponse}

import scala.concurrent.ExecutionContextExecutor

object RSSActor {
  def apply(botActor: ActorRef): Props = Props(classOf[RSSActor], botActor)
  case class PodcastEntry(title: String, pubDate: String, link: String, summary: String)
  case class TGResponse(text: String)
  case class Podcasts(podcasts: Seq[PodcastEntry], meta: PodcastMeta)
  case class PodcastMeta(logoUrl: String)
}

class RSSActor(podcastActor: ActorRef) extends Actor with Parser with Builder with Markup with StrictLogging {
  implicit val backend: SttpBackend[Id, Nothing] = HttpURLConnectionBackend()
  implicit val ec: ExecutionContextExecutor = context.dispatcher
  private var rssCache = Seq.empty[PodcastEntry]
  // @TODO
  private var meta = PodcastMeta("")
  private val soundCloudRSS = "feeds.soundcloud.com/users/soundcloud:users:306631331/sounds.rss"
  private val timer = new java.util.Timer()
  private val updateRSS = new java.util.TimerTask {
    override def run(): Unit = fetchFeed()
  }
  timer.schedule(updateRSS, 0, 60000 * 10000)

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    updateRSS.cancel()
  }

  def fetchFeed(): Unit = {
    import scala.xml.XML
    val rss = sttp.get(UriInterpolator.interpolate(StringContext(soundCloudRSS))).send()
    // @TODO: Handle exception
    val xmlRSS = XML.loadString(rss.body.right.get)
    rssCache = (xmlRSS \ "channel" \ "item").map(x => {
      val (text, links) = parse((x \ "summary").text)
      PodcastEntry((x \ "title").text, (x \ "pubDate").text, (x \ "link").text, text + "\n\n" + links)
    })
    meta = PodcastMeta((xmlRSS \ "channel" \ "image" \ "url").text)
    self ! NotUsed
    logger.info("cache updated")
  }

  override def receive: Receive = {
    case NotUsed =>
      logger.info("sent template to podcast actor")
      // @TODO Send only when changed
      podcastActor ! TGResponse(build(rssCache.head))
      podcastActor ! Podcasts(rssCache, meta)
  }
}
