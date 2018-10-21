name := "frontendu-telegram-bot"

version := "0.1"

scalaVersion := "2.12.7"

enablePlugins(JavaAppPackaging)

// Core with minimal dependencies, enough to spawn your first bot.
libraryDependencies += "com.bot4s" %% "telegram-core" % "4.0.0-RC2"
// Extra goodies: Webhooks, support for games, bindings for actors.
libraryDependencies += "com.bot4s" %% "telegram-akka" % "4.0.0-RC2"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.17"
libraryDependencies += "com.typesafe.akka" %% "akka-http"   % "10.1.5"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.17"

// https://mvnrepository.com/artifact/org.scala-lang.modules/scala-xml
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.1.1"
