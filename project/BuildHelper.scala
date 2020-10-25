import sbt._
import Keys._
import dotty.tools.sbtplugin.DottyPlugin.autoImport._
import scalafix.sbt.ScalafixPlugin.autoImport.scalafixSemanticdb
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object BuildHelper {

  val dottySettings = Seq(
    // Keep this consistent with the version in .circleci/config.yml
    crossScalaVersions := Version.dotty +: crossScalaVersions.value,
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
    },
    libraryDependencies := libraryDependencies.value.map(_.withDottyCompat(scalaVersion.value))
  )

  lazy val noPublish = skip in publish := true

  lazy val commonSettings = Seq(
    crossScalaVersions := Seq("2.12.12", Version.scala),
    version := "0.2.2",
    organization := "zio.crew",
    description := "Arrow interface for ZIO",
    startYear := Some(2020),
    homepage := Some(url("https://github.com/zio-crew/zio-arrow")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "tampler",
        "Boris V.Kuznetsov",
        "socnetfpga@gmail.com",
        url("https://github.com/tampler")
      )
    ),
    scmInfo := Some(
      ScmInfo(url("https://github.com/zio-crew/zio-arrow"), "scm:git@github.com:zio-crew/zio-arrow.git")
    ),
    maxErrors := 3,
    // Refine scalac params from tpolecat
    scalacOptions --= Seq(
      "-Xfatal-warnings"
    )
  )

  lazy val zioDeps = libraryDependencies ++= Seq(
    "dev.zio" %%% "zio" % Version.zio
  )

  lazy val zioTestDeps = libraryDependencies ++= Seq(
    "dev.zio" %%% "zio-test"     % Version.zio % "test",
    "dev.zio" %%% "zio-test-sbt" % Version.zio % "test"
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
