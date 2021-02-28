package com.machinedoll.play.streamingjoin

import java.sql.Timestamp

import org.apache.spark.sql.functions.{expr, struct, to_json}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

object StreamStream {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .appName("StreamStream")
      .master("local")
      .getOrCreate()

    import spark.implicits._

    spark.conf.set("spark.sql.streaming.checkpointLocation", "file:///tmp/example")

    val s9999: DataFrame = spark
      .readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 9999)
      .load()

    val s9999Dataset: Dataset[S9999] = s9999
      .map(line => {
        val strings = line.get(0).toString.split(",")
        val id = strings(0).toInt
        val time = Timestamp.valueOf(strings(1))
        S9999(id, time)
      })
      .withWatermark("timestamp99", "30 seconds")

    val s9998Dataset: Dataset[S9998] = spark
      .readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 9998)
      .load()
      .map(line => {
        val strings = line.get(0).toString.split(",")
        val id = strings(0).toInt
        val time = Timestamp.valueOf(strings(1))
        S9998(id, time)
      })
      .withWatermark("timestamp98", "10 seconds")


    val resultDataset = s9998Dataset
      .join(s9999Dataset,
        joinExprs = expr(
          """
                id99 = id98 AND
                timestamp98 >= timestamp99 AND
                timestamp98 <= timestamp99 + interval 6 seconds
        """),
        joinType = "left")

    val streamingQuery = resultDataset
      .select(
        to_json(
          struct($"id98", $"timestamp98", $"id99"))
          .alias("value"))
      .writeStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "chatbot001s-mbp:9092")
      .option("topic", "topic1")
      .start()

    streamingQuery.awaitTermination()
  }

  case class S9999(id99: Int, timestamp99: Timestamp)

  case class S9998(id98: Int, timestamp98: Timestamp)

}
