package st.youknow.updater

import akka.NotUsed
import akka.actor.{Actor, ActorRef, Cancellable, Props}
import com.softwaremill.sttp.{HttpURLConnectionBackend, Id, SttpBackend, UriInterpolator, sttp}
import slogging.StrictLogging
import st.youknow.updater.RSSActor.{PodcastEntry, PodcastMeta, PodcastsPayload}
import st.youknow.{Builder, Markup, Parser}
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

object RSSActor {
  def apply(botActor: ActorRef): Props = Props(classOf[RSSActor], botActor)
  case class PodcastEntry(title: String, pubDate: String, link: String, summary: String)
  case class Podcasts(podcasts: Seq[PodcastEntry] = Seq.empty[PodcastEntry])
  case class PodcastMeta(logoUrl: String = "")
  case class PodcastsPayload(podcasts: Seq[PodcastEntry], meta: PodcastMeta)
}

class RSSActor(podcastActor: ActorRef) extends Actor with Parser with Builder with Markup with StrictLogging {
  implicit val backend: SttpBackend[Id, Nothing] = HttpURLConnectionBackend()
  implicit val ec: ExecutionContextExecutor = context.dispatcher
  private val soundCloudRSS = "feeds.soundcloud.com/users/soundcloud:users:306631331/sounds.rss"

  val timer: Cancellable = context.system.scheduler.schedule(0.second, 1.minute) {
    self ! NotUsed
  }

  override def receive: Receive = {
    case NotUsed =>
      podcastActor ! fetchFeed()
      logger.info("cache updated")
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    timer.cancel()
  }

  def fetchFeed(): PodcastsPayload = {
    import scala.xml.XML
    val rss = sttp.get(UriInterpolator.interpolate(StringContext(soundCloudRSS))).send()
    // @TODO: Handle exception
    val xmlRSS = XML.loadString(rss.body.right.get)
    logger.info("send template to podcast actor")
    PodcastsPayload((xmlRSS \ "channel" \ "item").map(x => {
      val (text, links) = parse((x \ "summary").text)
      PodcastEntry((x \ "title").text, (x \ "pubDate").text, (x \ "link").text, text + "\n\n" + links)
    }), PodcastMeta((xmlRSS \ "channel" \ "image" \ "url").text))
  }
}
