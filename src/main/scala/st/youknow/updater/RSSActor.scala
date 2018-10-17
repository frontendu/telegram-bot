package st.youknow.updater

import akka.NotUsed
import akka.actor.{Actor, ActorRef, Props}
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

  context.system.scheduler.schedule(0.second, 1.minute) {
    self ! NotUsed
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
    val filteredText = text.stripMargin.split('\n').filterNot(_.trim.isEmpty)
    val summary = filteredText.filterNot(_.indexOf("http") > 0).mkString("\n")
    val links = filteredText.filter(_.indexOf("http") > 0).map { x =>
      val splitPos = x.indexOf("http")
      if (splitPos > 0) {
        val link = x.splitAt(splitPos - 1).productIterator.toList.map(_.toString.trim)
        s"[${link.head}](${link.last})"
      }
      else x
    }.mkString("\n")

    (summary, links)
  }

  def parseTitle(text: String): String = text.replace("#", "")
}
