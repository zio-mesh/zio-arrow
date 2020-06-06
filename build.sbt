resolvers ++= Seq(
  Resolver.mavenLocal,
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

inThisBuild(
  List(
    scalaVersion := "2.12.11",
    version := "0.2.1",
    organization := "io.github.neurodyne",
    description := "Arrow interface for ZIO",
    homepage := Some(url("https://github.com/Neurodyne/zio-arrow")),
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
      ScmInfo(url("https://github.com/Neurodyne/zio-arrow"), "scm:git@github.com:Neurodyne/zio-arrow.git")
    )
  )
)

lazy val commonSettings = Seq(
// Refine scalac params from tpolecat
  scalacOptions --= Seq(
    "-Xfatal-warnings"
  )
)

lazy val silencer = libraryDependencies ++= Seq(
  compilerPlugin("com.github.ghik" % "silencer-plugin" % Version.silencer cross CrossVersion.full),
  "com.github.ghik" % "silencer-lib" % Version.silencer % Provided cross CrossVersion.full
)

lazy val zioDeps = libraryDependencies ++= Seq(
  "dev.zio" %% "zio"          % Version.zio,
  "dev.zio" %% "zio-test"     % Version.zio % "test",
  "dev.zio" %% "zio-test-sbt" % Version.zio % "test"
)

lazy val graphDeps = libraryDependencies ++= Seq(
  "org.scala-graph" %% "graph-core" % Version.graph
)

lazy val bench = (project in file("bench"))
  .settings(commonSettings, skip.in(publish) := true, silencer)
  .enablePlugins(JmhPlugin)
  .dependsOn(root)

lazy val examples = (project in file("examples"))
  .settings(commonSettings, skip.in(publish) := true)
  .settings(graphDeps)
  .dependsOn(root)

lazy val root = (project in file("."))
  .settings(
    name := "zio-arrow",
    maxErrors := 3,
    commonSettings,
    zioDeps,
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )

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

publishTo := sonatypePublishToBundle.value

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

scalafixDependencies in ThisBuild += "com.nequissimus" %% "sort-imports" % "0.5.0"
