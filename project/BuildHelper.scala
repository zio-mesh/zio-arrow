import sbt._
import Keys._
import dotty.tools.sbtplugin.DottyPlugin.autoImport._
import scalafix.sbt.ScalafixPlugin.autoImport.scalafixSemanticdb

object BuildHelper {

  val dottyVersion = "0.26.0-bin-20200710-a162b7b-NIGHTLY"

  val dottySettings = Seq(
    // Keep this consistent with the version in .circleci/config.yml
    crossScalaVersions += dottyVersion,
    scalacOptions ++= {
      if (isDotty.value)
        Seq("-noindent")
      else
        Seq()
    },
    sources in (Compile, doc) := {
      val old = (Compile / doc / sources).value
      if (isDotty.value) {
        Nil
      } else {
        old
      }
    },
    parallelExecution in Test := {
      val old = (Test / parallelExecution).value
      if (isDotty.value) {
        false
      } else {
        old
      }
    }
  )

  lazy val commonSettings = Seq(
// Refine scalac params from tpolecat
    scalacOptions --= Seq(
      "-Xfatal-warnings"
    )
  )

  def stdSettings(prjName: String) = Seq(
    name := s"$prjName",
    crossScalaVersions := Seq("2.12.11", "2.13.3"),
    scalaVersion in ThisBuild := crossScalaVersions.value.head,
    libraryDependencies ++= {
      if (isDotty.value) {
        Seq(
          ("com.github.ghik" % "silencer-lib_2.13.3" % Version.silencer % Provided).withDottyCompat(scalaVersion.value)
        )

      } else
        Seq(
          "com.github.ghik" % "silencer-lib" % Version.silencer % Provided cross CrossVersion.full,
          compilerPlugin("com.github.ghik" % "silencer-plugin" % Version.silencer cross CrossVersion.full),
          compilerPlugin(scalafixSemanticdb)
        )
    },
    parallelExecution in Test := true
  )

  lazy val zioDeps = libraryDependencies ++= Seq(
    "dev.zio" %% "zio"          % Version.zio,
    "dev.zio" %% "zio-test"     % Version.zio % "test",
    "dev.zio" %% "zio-test-sbt" % Version.zio % "test"
  )

  lazy val graphDeps = libraryDependencies ++= Seq(
    "org.scala-graph" %% "graph-core" % Version.graph
  )

  lazy val silencer = libraryDependencies ++= {
    if (isDotty.value) {
      Seq(("com.github.ghik" % "silencer-lib_2.13.3" % Version.silencer % Provided).withDottyCompat(scalaVersion.value))

    } else {
      Seq(
        compilerPlugin("com.github.ghik" % "silencer-plugin" % Version.silencer cross CrossVersion.full),
        "com.github.ghik" % "silencer-lib" % Version.silencer % Provided cross CrossVersion.full
      )
    }
  }

}
