import BuildHelper._

resolvers ++= Seq(
  Resolver.mavenLocal,
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

inThisBuild(
  List(
    scalaVersion := "2.13.3",
    crossScalaVersions := Seq("2.12.11", "2.13.3", dottyVersion),
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
    )
  )
)

lazy val compat = (project in file("compat"))
  .settings(dottySettings)
  .settings(commonSettings, skip.in(publish) := true)

lazy val bench = (project in file("bench"))
  .settings(dottySettings)
  .settings(commonSettings, skip.in(publish) := true, silencer)
  .settings(libraryDependencies := libraryDependencies.value.map(_.withDottyCompat(scalaVersion.value)))
  .enablePlugins(JmhPlugin)
  .dependsOn(root)

lazy val examples = (project in file("examples"))
  .settings(dottySettings)
  .settings(commonSettings, skip.in(publish) := true)
  .settings(graphDeps)
  .settings(libraryDependencies := libraryDependencies.value.map(_.withDottyCompat(scalaVersion.value)))
  .dependsOn(root)

lazy val root = (project in file("."))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(dottySettings)
  .settings(
    name := "zio-arrow",
    maxErrors := 3,
    commonSettings,
    zioDeps,
    libraryDependencies := libraryDependencies.value.map(_.withDottyCompat(scalaVersion.value)),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
  )
  .dependsOn(compat)

lazy val docs = project // new documentation project
  .in(file("zio-arrow-docs"))
  .settings(
    skip.in(publish) := true,
    moduleName := "zio-arrow-docs",
    scalacOptions -= "-Yno-imports",
    scalacOptions -= "-Xfatal-warnings",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % Version.zio
    )
  )
  .dependsOn(root)
  .enablePlugins(MdocPlugin)

// Common
addCommandAlias("rel", "reload")
addCommandAlias("com", "all compile test:compile it:compile")
addCommandAlias("fix", "all compile:scalafix test:scalafix")
addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")
addCommandAlias("cov", "; clean; coverage; test; coverageReport")
addCommandAlias("pub", "; publishSigned; sonatypeBundleRelease")

// Benchmarks
addCommandAlias("benchApi", "bench/jmh:run -i 1 -wi 1 -f1 -t2 .*ApiBenchmark")
addCommandAlias("benchArray", "bench/jmh:run -i 1 -wi 1 -f1 -t2 ;.*BubbleSortBenchmark;.*ArrayFillBenchmark")
addCommandAlias("benchSocket", "bench/jmh:run -i 1 -wi 1 -f1 -t2 .*SocketBenchmark")
addCommandAlias("benchCompute", "bench/jmh:run -i 1 -wi 1 -f1 -t2 .*ComputeBenchmark")

scalafixDependencies in ThisBuild += "com.nequissimus" %% "sort-imports" % "0.5.4"
