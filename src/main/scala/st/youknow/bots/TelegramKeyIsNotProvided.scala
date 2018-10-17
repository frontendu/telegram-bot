package st.youknow.bots

final case class TelegramKeyIsNotProvided(private val message: String, private val cause: Throwable = None.orNull)
  extends Exception(message, cause)