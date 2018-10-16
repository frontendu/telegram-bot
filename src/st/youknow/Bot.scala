import akka.actor.{ActorSystem, Props}
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.concurrent.duration.Duration

object Bot extends App {
  val system = ActorSystem("default")
  //  val actor = system.actorOf(Props[RSSActor], "default")
  //  implicit val ec: ExecutionContextExecutor = system.dispatcher

  //  val b = new GetLastPodcastBot("").run()


  //  actorSystem.scheduler.schedule(0.second, 1.minute)(actor ! Nil)

  //  Await.result(b, Duration.Inf)
}
