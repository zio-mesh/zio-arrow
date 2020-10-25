import BuildHelper._

resolvers ++= Seq(
  Resolver.mavenLocal,
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

inThisBuild(scalaVersion := Version.scala)

lazy val root = (project in file("."))
  .settings(noPublish)
  .aggregate(arrowJVM, arrowJS, examples, bench, docs)

val arrow = crossProject(JVMPlatform, JSPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("zio-arrow"))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(
    name := "zio-arrow",
    commonSettings,
    zioDeps,
    zioTestDeps,
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
  .jvmSettings(
    dottySettings
  )

lazy val arrowJS  = arrow.js
lazy val arrowJVM = arrow.jvm

lazy val bench = (project in file("bench"))
  .settings(
    commonSettings,
    noPublish,
    silencer,
    dottySettings
  )
  .enablePlugins(JmhPlugin)
  .dependsOn(arrowJVM)

lazy val examples = (project in file("examples"))
  .settings(
    commonSettings,
    noPublish,
    graphDeps,
    dottySettings
  )
  .dependsOn(arrowJVM)

lazy val docs = project // new documentation project
  .in(file("zio-arrow-docs"))
  .settings(
    commonSettings,
    noPublish,
    moduleName := "zio-arrow-docs",
    scalacOptions -= "-Yno-imports",
    scalacOptions -= "-Xfatal-warnings",
    dottySettings
  )
  .dependsOn(arrowJVM)
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
