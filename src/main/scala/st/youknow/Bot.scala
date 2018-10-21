package st.youknow

import akka.actor.ActorSystem
import st.youknow.bots.Podcast
import st.youknow.updater.RSSActor
import scala.util.Properties.envOrNone

object Bot extends App {
  val system = ActorSystem()
  val b = system.actorOf(Podcast(envOrNone("FRONTENDU_TG_KEY")))
  val rssActor = system.actorOf(RSSActor(b))
}
