scalaVersion in ThisBuild := "2.11.8"

name := "Spark Stateful Streaming"

lazy val excludeJpountz = ExclusionRule(organization = "net.jpountz.lz4", name = "lz4")

lazy val kafkaClients = "org.apache.kafka" % "kafka-clients" % "2.4.0" excludeAll(excludeJpountz)

libraryDependencies ++= Seq(
  "com.github.melrief" %% "pureconfig" % "0.6.0",
  "org.apache.spark" %% "spark-streaming" % "2.4.0",
  "org.apache.spark" %% "spark-sql" % "2.4.0",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "2.4.0",
  "org.lz4" % "lz4-java" % "1.7.1",
  "io.argonaut" %% "argonaut" % "6.1"
)