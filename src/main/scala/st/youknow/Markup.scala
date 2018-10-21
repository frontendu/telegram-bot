package st.youknow

import com.bot4s.telegram.models.{InlineKeyboardButton, InlineKeyboardMarkup}

trait Markup {
  def listenButton(link: String): InlineKeyboardMarkup = InlineKeyboardMarkup.singleButton(
    InlineKeyboardButton.url("\n\ud83c\udfa7 Слушать подкаст \ud83c\udfa7", link))
}
