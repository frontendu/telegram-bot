name := "frontendu-telegram-bot"

version := "0.1"

scalaVersion := "2.12.7"

enablePlugins(JavaAppPackaging)

// Core with minimal dependencies, enough to spawn your first bot.
libraryDependencies += "com.bot4s" %% "telegram-core" % "4.0.0-RC1"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.17"

// Выпилить нахрен
libraryDependencies += "com.typesafe.akka" %% "akka-http"   % "10.1.5"

libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.17"

libraryDependencies += "com.softwaremill.sttp" %% "core" % "1.3.8"

// https://mvnrepository.com/artifact/org.scala-lang.modules/scala-xml
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.1.1"
