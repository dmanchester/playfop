name := """sample-scala"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.dmanchester" %% "playfop" % "0.4-SNAPSHOT",
  "com.adrianhurt" %% "play-bootstrap3" % "0.4.4-P23"
)
