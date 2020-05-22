package services.soundcloud

import com.fasterxml.jackson.annotation.JsonRootName

data class AllPodcasts(val channel: Podcasts)

@JsonRootName("channel")
data class Podcasts(val item: List<Podcast>)

@JsonRootName("item")
data class Podcast(val title: String, val link: String, val description: String)

fun AllPodcasts.getLastPodcast(): Podcast = this.channel.item.first()

fun PodcastMessage.getPodcastNumber(): Int = this.body.title.filter { it.isDigit() }.toInt()
