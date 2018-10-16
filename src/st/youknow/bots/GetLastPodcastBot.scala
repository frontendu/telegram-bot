package bots

import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.api.Polling
import com.bot4s.telegram.clients.ScalajHttpClient
import scala.util.Try

class GetLastPodcastBot(override val token: String) extends AbstractBot(token: String)
  with Polling
  with Commands {
  override val client = new ScalajHttpClient(token)
  onCommand("get_last_podcast") { implicit msg =>
    reply("последний подкаст")
  }

  /* Int(n) extractor */
  object Int { def unapply(s: String): Option[Int] = Try(s.toInt).toOption }
}
