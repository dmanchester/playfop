// In a typical sbt project, task "publishLocal" publishes locally, and
// task "publish" does so remotely. In this one, both do so locally.
// "publishLocal" is intended primarily for snapshot builds of just the
// PlayFOP JAR. "publish" is intended for release builds of that JAR, and of
// other artifacts.

lazy val publishDir = settingKey[File]("The directory to which 'publish...' tasks (except 'publishLocal') publish artifacts.")

lazy val cleanAll = taskKey[Unit]("Cleans all projects and deletes publishDir.")

lazy val publishAll = taskKey[Unit]("Publishes all six artifacts to publishDir.")

lazy val publishAndFlatten = taskKey[Unit]("""Builds and publishes to publishDir the PlayFOP JAR
                                             |and POM, as well as a JAR of sources. The directory
                                             |structure that would normally accompany publication is
                                             |flattened such that all files are placed at the top
                                             |level of publishDir.""".stripMargin.replaceAll("\n", " "))

lazy val publishDoc = taskKey[Unit]("""To publishDir, publishes Scaladoc, Javadoc, or the user guide
                                      |(depending on the project).""".stripMargin.replaceAll("\n", " "))

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
      cleanAll := {
        clean.value
        (clean in scaladocOnly).value
        (clean in javadocOnly).value
        (clean in userguide).value
        IO.delete(publishDir.value)
      },
      publishAll := {
        publishAndFlatten.value
        (publishDoc in scaladocOnly).value
        (publishDoc in javadocOnly).value
        (publishDoc in userguide).value
      },
      publishTo := Some(Resolver.file("file", publishDir.value)),
      publishArtifact in (Compile, packageDoc) := false,  // don't publish Javadocs from this project; look to "javadocOnly" project for that
      publishAndFlatten := {
        publish.value
        val organizationPart1 = organization.value.split("\\.").head  // extracts "com" from "com.dmanchester"
        val jarAndPomFinder = publishDir.value / organizationPart1 ** ("*.jar" || "*.pom")
        val jarsAndPom = jarAndPomFinder.get

        val log = streams.value.log
        log.info("Flattening " + publishDir.value + "...")
        IO.move(jarsAndPom.map { srcFile =>
            srcFile -> (publishDir.value / srcFile.getName())
        })
        IO.delete(publishDir.value / organizationPart1)
      },
      // Additional information for inclusion in POM file
      licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
      homepage := Some(url("https://www.dmanchester.com/playfop")),
      scmInfo := Some(ScmInfo(url("https://github.com/dmanchester/playfop"),
                                  "git@github.com:dmanchester/playfop.git")),
      developers := List(Developer("dpmanchester",
                                   "Daniel Manchester",
                                   "dpmanchester@gmail.com",
                                   url("https://www.dmanchester.com/")))
  )

lazy val scaladocOnly = project.
  settings(commonSettings: _*).
  settings(
    scalaSource in Compile := file("./src/main/scala"),
    target := file("./target-scaladoc"),
    publishDoc := {
      val docJarSource = (packageDoc in Compile).value
      val docJarTargetName = docJarSource.getName().replace("javadoc", "scaladoc")  // address anomaly in JAR name: even though it contains Scaladoc, name suggests Javadoc
      val docJarTarget = new File(publishDir.value, docJarTargetName)

      val log = streams.value.log
      log.info("Copying " + docJarSource + " to " + docJarTarget + "...")
      IO.copyFile(docJarSource, docJarTarget)
    }
  )

lazy val javadocOnly = project.
  settings(commonSettings: _*).
  settings(
    javaSource in Compile := file("./src/main/java"),
    javacOptions in Compile ++= Seq("-Xdoclint:all,-html"),
    target := file("./target-javadoc"),
    publishDoc := {
      val docJarSource = (packageDoc in Compile).value
      val docJarTarget = new File(publishDir.value, docJarSource.getName())

      val log = streams.value.log
      log.info("Copying " + docJarSource + " to " + docJarTarget + "...")
      IO.copyFile(docJarSource, docJarTarget)
    }
  )

lazy val userguide = scalatex.ScalatexReadme(
  projectId = "userguide",
  wd = file(""),
  url = "https://github.com/dmanchester/playfop/tree/master",
  source = "UserGuide"
).settings(commonSettings: _*).  // only common setting we need is publishDir
  settings(
    publishDoc := {
      (run in Compile).toTask("").value
      val userguideDir = target.value / "scalatex"
      val zipFile = name.value + "-" + version.value + "-userguide.zip"
      val zipFileWithPath = publishDir.value / zipFile

      val log = streams.value.log
      log.info("Zipping " + userguideDir + " to " + zipFileWithPath + "...")
      val fileFinder = userguideDir ** "*.*"
      val filesToZip = fileFinder.get

      IO.zip(filesToZip.map { srcFile =>
        val srcFileWithoutUserguideDir = srcFile.getPath().substring(userguideDir.getPath().length())  // represent directories in zip relative to userguideDir
        srcFile -> srcFileWithoutUserguideDir
      }, zipFileWithPath)
    }
  )

resolvers += ("JBoss" at "https://repository.jboss.org/nexus/content/groups/public")

lazy val commonSettings = Seq(
  name := "playfop",
  description := """A library for generating PDFs, images, and other types of output in
                   |Play Framework applications. Accepts XSL-FO that an application has
                   |generated--via a Play Twirl template, with the scala-xml library, or as a
                   |String--and processes it with Apache FOP.""".stripMargin,
  organization := "com.dmanchester",
  version := "1.1-SNAPSHOT",
  scalaVersion := "2.13.4",
  crossScalaVersions := Seq("2.12.12", "2.13.4"),
  autoScalaLibrary := false,
  publishDir := new File("./dist-" + CrossVersion.binaryScalaVersion(scalaVersion.value)),
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-library" % scalaVersion.value % "provided",
    "com.typesafe.play" %% "play" % scalaVersionToPlayVersion(scalaVersion.value) % "provided",
    "org.apache.xmlgraphics" % "fop" % "2.5",
    "org.specs2" %% "specs2-core" % "4.8.3" % "test",
    "org.apache.commons" % "commons-collections4" % "4.4" % "test",
    "com.michaelpollmeier" %% "scala-arm" % "2.1" % "test",
    // The following PlayFOP dependencies are shared with other libraries
    // specified in this file. We allow those libraries' use of them to
    // determine the version numbers below. Consult
    // ".../target/scala-2.12/resolution-cache/reports" for more information.
    "com.typesafe.play" %% "twirl-api" % "1.5.0" % "provided",
    "org.slf4j" % "slf4j-api" % "1.7.30",
    "org.scala-lang.modules" %% "scala-xml" % "1.2.0",
    "junit" % "junit" % "4.13" % "test",
    "com.novocode" % "junit-interface" % "0.11" % "test",
    "ch.qos.logback" % "logback-classic" % "1.2.3" % "test",
    "org.apache.pdfbox" % "pdfbox" % "2.0.22" % "test"
    // ...end shared dependencies.
  ),
  scalacOptions ++= Seq("-deprecation", "-feature"),  // per http://alvinalexander.com/scala/scala-sbt-re-run-with-deprecation-feature-message
  scalacOptions in Compile in doc ++= Seq("-doc-root-content", "doc-root-content.html"),
  scalacOptions in Test ++= Seq("-Yrangepos")  // per https://etorreborre.github.io/specs2/website/SPECS2-3.9.1/quickstart.html
)

/** For a given Scala version, returns a Play version.
  *
  * The "major" portion of the Play version is the earliest one that PlayFOP
  * seeks to support for that Scala version: Play 2.6.x for Scala 2.12;
  * Play 2.4.x for Scala 2.11.
  *
  * The "minor" portion of the Play version simply seeks to be recent.
  */
lazy val scalaVersionToPlayVersion = { scalaVersion: String =>

  CrossVersion.binaryScalaVersion(scalaVersion) match {
    case "2.12" | "2.13" => "2.8.2"
    case _ => throw new UnsupportedOperationException(s"Unsupported Scala version: '$scalaVersion'")
  }
}
