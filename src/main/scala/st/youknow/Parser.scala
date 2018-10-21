package st.youknow

trait Parser {
  def parse(text: String): (String, String) = {
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

  def parseTitle(text: String): String = text.replace("#", "")

  private def renderLink(str: String): String = {
    val (desc, link) = str.splitAt(str.indexOf("http")) // todo: use regexp
    s"[${desc.trim}]($link)"
  }
}
