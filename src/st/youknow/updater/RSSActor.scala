package updater

import akka.actor.Actor
import com.softwaremill.sttp.{HttpURLConnectionBackend, Id, SttpBackend, UriInterpolator, sttp}
import akka.http.scaladsl.model.StatusCodes


class RSSActor extends Actor {
  case class LastPodcast(title: String, pubDate: String, link: String, summary: String)

  private val soundCloudRSS = "feeds.soundcloud.com/users/soundcloud:users:306631331/sounds.rss"

  override def receive: Receive = {
    case _ =>
      implicit val backend: SttpBackend[Id, Nothing] = HttpURLConnectionBackend()
      val rss = sttp.get(UriInterpolator.interpolate(StringContext(soundCloudRSS))).send()
      if (rss.code != StatusCodes.OK.intValue) {
        // @TODO: Handle exception
      }

      val xml = (scala.xml.XML.loadString(rss.body.right.get) \ "channel" \ "item").head
      val entry = LastPodcast((xml \ "title").text, (xml \ "pubDate").text, (xml \ "link").text, (xml \ "summary").text)

      val template =
        s"""
        ## ${entry.title}
        # ${entry.pubDate}
          ${entry.summary}
           |[Слушать](${entry.link}
        """

      println(template)
  }
}
