ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.7.0"

val Versions = new {
  val Pekko = "1.1.3"
}

resolvers += "Local Ivy Repo" at "file://" + Path.userHome.absolutePath + "/.ivy2/local"

lazy val root =
  project
    .in(file("."))
    .settings(
      name := "grpc-stream-tyk-issue-reproducer",
      libraryDependencies ++= Seq(
        "org.apache.pekko" %% "pekko-actor-typed" % Versions.Pekko,
        "org.apache.pekko" %% "pekko-stream" % Versions.Pekko,
        "org.apache.pekko" %% "pekko-discovery" % Versions.Pekko,
        "ch.qos.logback" % "logback-classic" % "1.5.18"
      ),
      pekkoGrpcCodeGeneratorSettings += "server_power_apis",
      assembly / assemblyJarName := "server.jar",
      assembly / assemblyMergeStrategy := {
        case PathList("META-INF", xs@_*) =>
          xs map {
            _.toLowerCase
          } match {
            case "services" :: xs =>
              MergeStrategy.filterDistinctLines
            case _ => MergeStrategy.discard
          }
        case PathList("module-info.class") =>
          MergeStrategy.last
        case path if path.endsWith("/module-info.class") =>
          MergeStrategy.last
        case PathList(file) if file.endsWith(".conf") =>
          MergeStrategy.concat
        case _ =>
          MergeStrategy.first
      },
    )
    .enablePlugins(PekkoGrpcPlugin)
