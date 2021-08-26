ThisBuild / resolvers ++= Seq(
    "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
    Resolver.mavenLocal
)

name := "flink-cdc"

version := "0.1-SNAPSHOT"

organization := "com.whitilied"

ThisBuild / scalaVersion := "2.12.14"

scalacOptions += "-target:jvm-1.8"

val flinkVersion = "1.13.2"

val flinkScope = "provided"

val flinkDependencies = Seq(
  "org.apache.flink" %% "flink-clients" % flinkVersion % flinkScope,
  "org.apache.flink" %% "flink-scala" % flinkVersion % flinkScope,
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % flinkScope,
  // FLINK TABLE
  "org.apache.flink" %% "flink-table-api-scala-bridge" % flinkVersion % flinkScope,
  "org.apache.flink" % "flink-table-common" % flinkVersion % flinkScope,
  "org.apache.flink" %% "flink-table-planner-blink" % flinkVersion % flinkScope,
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % flinkScope,
  "org.apache.flink" %% "flink-connector-jdbc" % flinkVersion,
  "org.apache.flink" %% "flink-statebackend-rocksdb" % flinkVersion,
  // CDC
  "com.ververica" % "flink-sql-connector-mysql-cdc" % "2.0.0",
  // DB
  "mysql" % "mysql-connector-java" % "8.0.26",
  // Other
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.10.5",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.10.5",
)

lazy val root = (project in file(".")).
  settings(
    libraryDependencies ++= flinkDependencies
  )

assembly / mainClass := Some("com.whitilied.DataAuditExample")

// make run command include the provided dependencies
Compile / run  := Defaults.runTask(Compile / fullClasspath,
                                   Compile / run / mainClass,
                                   Compile / run / runner
                                  ).evaluated

// stays inside the sbt console when we press "ctrl-c" while a Flink programme executes with "run" or "runMain"
Compile / run / fork := true
Global / cancelable := true

// exclude Scala library from assembly
assembly / assemblyOption  := (assembly / assemblyOption).value.copy(includeScala = false)

ThisBuild / assemblyMergeStrategy := {
  case PathList("org","aopalliance", xs @ _*) => MergeStrategy.last
  case PathList("javax", "inject", xs @ _*) => MergeStrategy.last
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
  case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
  case PathList("org", "apache", xs @ _*) => MergeStrategy.last
  case PathList("com", "google", xs @ _*) => MergeStrategy.last
  case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
  case PathList("com", "codahale", xs @ _*) => MergeStrategy.last
  case PathList("com", "yammer", xs @ _*) => MergeStrategy.last
  case PathList("com", "fasterxml", xs @ _*) => MergeStrategy.last
  case PathList("com", "mysql", xs @ _*) => MergeStrategy.last
  case "about.html" => MergeStrategy.rename
  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
  case "META-INF/mailcap" => MergeStrategy.last
  case "META-INF/INFO_BIN" => MergeStrategy.last
  case "META-INF/INFO_SRC" => MergeStrategy.last
  case "META-INF/mimetypes.default" => MergeStrategy.last
  case "plugin.properties" => MergeStrategy.last
  case "log4j.properties" => MergeStrategy.last
  case "module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
