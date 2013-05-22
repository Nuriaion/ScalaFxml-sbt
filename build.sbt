scalaVersion := "2.9.2"

sbtPlugin := true

name := "scalafxml-sbt"

organization := "com.github.nuriaion"

//scalacOptions ++= Seq(
//    "-feature",
//    "-deprecation"
//)

version :="0.1.0-SNAPSHOT"

parallelExecution := true

resolvers ++= Seq(
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
    "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
    "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
    "Nuriaion Snapshots" at "http://nuriaion.github.io/maven-repo/maven-repo/snapshots",
    "Nuriaion Releases" at "http://nuriaion.github.io/maven-repo/maven-repo/releases"
)

libraryDependencies ++= Seq(
    "com.eed3si9n" %% "treehugger" % "0.2.3",
    "org.scalaz" %% "scalaz-core" % "7.0.0"
)

libraryDependencies ++= Seq(
    "org.specs2" %% "specs2" % "1.12.3" % "test",
    "org.scalatest" %% "scalatest" % "1.9.1" % "test",
    "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
    "junit" % "junit" % "4.11" % "test",
    "org.pegdown" % "pegdown" % "1.2.0" % "test"
)

libraryDependencies ++= Seq(
    "com.github.nuriaion" %% "scalafxml" % "0.1.0-SNAPSHOT"
)

testOptions in Test += Tests.Argument("junitxml", "html", "console", "markup")

publishMavenStyle := true

publishTo := Some(Resolver.file("file", new File("../maven-repo/maven-repo/snapshots")))

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

licenses := Seq("BSD-style" -> url("http://www.opensource.org/licenses/bsd-license.php"))

homepage := Some(url("https://github.com/Nuriaion/ScalaFxml-sbt"))