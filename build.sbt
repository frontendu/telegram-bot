name := "frontendu-telegram-bot"

version := "0.1"

scalaVersion := "2.12.7"

// Core with minimal dependencies, enough to spawn your first bot.
libraryDependencies += "com.bot4s" %% "telegram-core" % "4.0.0-RC1"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.17"
libraryDependencies += "com.typesafe.akka" %% "akka-http"   % "10.1.5"

libraryDependencies += "com.softwaremill.sttp" %% "core" % "1.3.8"
