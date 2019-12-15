import Dependencies._

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

ThisBuild / javacOptions ++= Seq("-source", "13")

lazy val root = (project in file("."))
  .settings(  
    name := "play-jdk13-alpine-slim",

    // disable javadoc/scaladoc generation
    sources in (Compile, doc) := Seq.empty,
    publishArtifact in (Compile, packageDoc) := false,

    // Rely on akkaHttpServer and logback, nothing else.
    libraryDependencies += akkaHttpServer,
    libraryDependencies += logback,
    
    //-------------------------------------
    // Docker settings
    //-------------------------------------
    dockerExposedPorts ++= Seq(9000),

    // Point the Play logs at the right place.
    Docker / defaultLinuxLogsLocation := "/opt/docker/logs",
    dockerExposedVolumes := Seq((Docker / defaultLinuxLogsLocation).value),

    // Always use latest tag
    dockerUpdateLatest := true,

    // Don't let Docker write out a PID file to /opt/docker, there's no write access,
    // and it doesn't matter anyway.
    dockerEnvVars := Map(
      "JAVA_TOOL_OPTIONS" -> "-Dpidfile.path=/dev/null"
    ),

    // Use alpine image that has been stripped down
    dockerBaseImage := "adoptopenjdk/openjdk13:alpine-slim",

    // Use the fancy new GC in JDK 13
    javaOptions in Universal ++= Seq(
      "-J-Xmx512m",
      "-J-Xms512m",
      "-J-XX:+UnlockExperimentalVMOptions",
      "-J-XX:+UseZGC",
    ),

    libraryDependencies += scalaTest % Test
  ).enablePlugins(PlayService, DockerPlugin, AshScriptPlugin)

