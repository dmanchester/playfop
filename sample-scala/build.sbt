name := """sample-scala"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

routesGenerator := InjectedRoutesGenerator

libraryDependencies ++= Seq(
  "com.dmanchester" %% "playfop" % "0.9-SNAPSHOT",
  "com.adrianhurt" %% "play-bootstrap" % "1.1.1-P24-B3"
)
