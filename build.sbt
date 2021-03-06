name := "slack-bot-jira"

version := "0.1"

scalaVersion := "2.12.5"

libraryDependencies ++= Seq("com.github.gilbertw1" %% "slack-scala-client" % "0.2.3",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.10",
  "com.typesafe" % "config" % "1.3.3")
