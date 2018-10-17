package st.youknow.bots

import akka.actor.{Actor, Props}
import com.bot4s.telegram.api.Polling
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.clients.ScalajHttpClient
import st.youknow.updater.RSSActor.TGResponse
import scala.util.Try

object GetLastPodcastBot {
  def apply(token: Option[String]): Props = {
    if (token.isEmpty) throw TelegramKeyIsNotProvided("Telegram key not found. Set it by FRONTENDU_TG_KEY env variable")
    Props(classOf[GetLastPodcastBot], token.get)
  }
}

class GetLastPodcastBot(override val token: String) extends AbstractBot(token: String) with Actor
  with Polling
  with Commands {
  override val client = new ScalajHttpClient(token)
  private var maybePodcast: Option[String] = None

  override def preStart: Unit = {
    super.preStart()
    run()
  }

  onCommand("get_last_podcast") { implicit msg =>
    replyMd(maybePodcast.getOrElse("Ты настолько быстр, что я не успел получить RSS!"))
  }

  override def receive: Receive = {
    case TGResponse(text) =>
      maybePodcast = Some(text)
  }

  /* Int(n) extractor */
  object Int { def unapply(s: String): Option[Int] = Try(s.toInt).toOption }
}
