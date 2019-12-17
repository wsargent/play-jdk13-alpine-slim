import Dependencies._
import com.typesafe.sbt.packager.docker.ExecCmd

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

    // JMX access only through running hawt.io, no JMX over RMI
    // https://jolokia.org/reference/html/agents.html#agents-jvm
    // https://mvnrepository.com/artifact/org.jolokia/jolokia-agent-jvm
    javaAgents += JavaAgent( "org.jolokia" % "jolokia-agent-jvm" % "2.0.0-M3" classifier "agent", arguments = "host=0.0.0.0,port=8778"),

    // Rely on akkaHttpServer and logback, nothing else.
    libraryDependencies += akkaHttpServer,
    libraryDependencies += logback,

      //-------------------------------------
    // Docker settings
    //-------------------------------------
    dockerExposedPorts ++= Seq(9000, 8778),

    // Point the Play logs at the right place.
    Docker / defaultLinuxLogsLocation := "/opt/docker/logs",
    dockerExposedVolumes := Seq((Docker / defaultLinuxLogsLocation).value),

    // Always use latest tag
    dockerUpdateLatest := true,

    // Don't let Docker write out a PID file to /opt/docker, there's no write access,
    // and it doesn't matter anyway.
    dockerEnvVars := Map(
      "JAVA_TOOL_OPTIONS" -> "-Dpidfile.path=/dev/null",
      "LOG_DIR" -> (Docker / defaultLinuxLogsLocation).value
    ),

    // Use alpine image that has been stripped down
    dockerBaseImage := "adoptopenjdk/openjdk13:alpine-slim",

    // Use the fancy new GC in JDK 13
    javaOptions in Universal ++= Seq(
      "-Djdk.serialFilter='!*'",   // blacklist all java serialization
      "-J-Xmx512m",
      "-J-Xms512m",
      "-J-XX:+UnlockExperimentalVMOptions",
      "-J-XX:+UseZGC",
      "-J-XX:+UnlockDiagnosticVMOptions",
      "-J-XX:+HeapDumpOnOutOfMemoryError",
    ),

    // Add JFR, GC logging and heap dumps
    bashScriptExtraDefines += """addJava "-Xlog:gc*=debug:file=$LOG_DIR/gc.log:utctime,uptime,tid,level:filecount=10,filesize=128m"""",
    bashScriptExtraDefines += """addJava "-XX:HeapDumpPath=$LOG_DIR/heapdump.hprof"""",
    bashScriptExtraDefines += """addJava "-XX:StartFlightRecording=disk=true,dumponexit=true,filename=$LOG_DIR/recording.jfr,maxsize=1024m,maxage=1d,settings=profile,path-to-gc-roots=true"""",

    libraryDependencies += scalaTest % Test
  ).enablePlugins(PlayService, JavaAgent, DockerPlugin, AshScriptPlugin)

