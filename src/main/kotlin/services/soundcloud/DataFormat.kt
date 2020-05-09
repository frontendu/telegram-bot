package services.soundcloud

import com.fasterxml.jackson.annotation.JsonRootName

data class Message(val channel: Podcasts)

@JsonRootName("channel")
data class Podcasts(val item: List<Podcast>)

@JsonRootName("item")
data class Podcast(val title: String, val link: String, val description: String)

fun Message.getLastPodcast(): Podcast = this.channel.item.first()

fun Podcast.getPodcastNumber(): Int = this.title.filter { it.isDigit() }.toInt()
