package st.youknow

import st.youknow.updater.RSSActor.PodcastEntry

trait Builder extends Parser {
  def build(podcast: PodcastEntry): String = {
    s"""
       |*${parseTitle(podcast.title)}*
       |
       |[Слушать подкаст](${podcast.link})
       |
       |${podcast.summary}
         """.stripMargin
  }
}
