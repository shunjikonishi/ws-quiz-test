name := "ws-quiz-test"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "org.java-websocket" % "Java-WebSocket" % "1.3.0"
)     

play.Project.playScalaSettings
