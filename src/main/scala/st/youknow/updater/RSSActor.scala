package st.youknow.updater

import akka.NotUsed
import akka.actor.{Actor, ActorRef, Cancellable, Props}
import com.softwaremill.sttp.{HttpURLConnectionBackend, Id, SttpBackend, UriInterpolator, sttp}

import scala.concurrent.ExecutionContextExecutor
import st.youknow.updater.RSSActor.{LastPodcast, TGResponse}

import scala.concurrent.duration._

object RSSActor {
  def apply(botActor: ActorRef): Props = Props(classOf[RSSActor], botActor)

  case class LastPodcast(title: String, pubDate: String, link: String, summary: String)

  case class TGResponse(text: String)

}

class RSSActor(botActor: ActorRef) extends Actor {
  implicit val backend: SttpBackend[Id, Nothing] = HttpURLConnectionBackend()
  implicit val ec: ExecutionContextExecutor = context.dispatcher
  private val soundCloudRSS = "feeds.soundcloud.com/users/soundcloud:users:306631331/sounds.rss"

  val timer: Cancellable = context.system.scheduler.schedule(0.second, 1.minute) {
    self ! NotUsed
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    timer.cancel()
  }

  override def receive: Receive = {
    case NotUsed =>
      val rss = sttp.get(UriInterpolator.interpolate(StringContext(soundCloudRSS))).send()
      // @TODO: Handle exception

      val xml = (scala.xml.XML.loadString(rss.body.right.get) \ "channel" \ "item").head
      val entry = LastPodcast((xml \ "title").text, (xml \ "pubDate").text, (xml \ "link").text, (xml \ "summary").text)

      val parsedSummary = parseSummary(entry.summary)
      val template =
        s"""
           |*${parseTitle(entry.title)}*
           |
           |[Слушать подкаст](${entry.link})
           |
           |${parsedSummary._1}
           |
           |${parsedSummary._2}
         """.stripMargin

      botActor ! TGResponse(template)
  }



  def parseSummary(text: String): (String, String) = {
    val (links, texts) = text.split("\n").view
      .map(_.trim.replaceAll("""\s{2,}""", " "))
      .filterNot(_.isEmpty)
      .foldLeft(List.empty[String] -> List.empty[String]) {
        // todo use regexp
        case ((ls, ts), line) if line.contains("http") => (renderLink(line) +: ls) -> ts
        case ((ls, ts), line) => ls -> (line +: ts)
      }

    (texts.reverse.mkString("\n"), links.reverse.mkString("\n"))
  }

  private def renderLink(str: String): String = {
    val (desc, link) = str.splitAt(str.indexOf("http")) // todo: use regexp
    s"[${desc.trim}]($link)"
  }

  private def parseTitle(text: String): String = text.replace("#", "")
}
