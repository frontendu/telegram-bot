package st.youknow

import akka.actor.ActorSystem
import st.youknow.bots.GetLastPodcastBot
import st.youknow.updater.RSSActor
import util.Properties.envOrNone

object Bot extends App {
  val system = ActorSystem()
  val b = system.actorOf(GetLastPodcastBot(envOrNone("FRONTENDU_TG_KEY")))
  val rssActor = system.actorOf(RSSActor(b))
}
