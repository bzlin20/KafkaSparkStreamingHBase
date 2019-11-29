package com.bonc.qyl.Spark

import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  *  SparkStreaming 连接Kafka集群
*/

object SparkStreamingDirectKafka {
  def main(args: Array[String]): Unit = {
      /**
        * 建立连接 先用local进行测试
      */
      System.setProperty("HADOOP_USER_NAME","qyl")
      val conf = new SparkConf().setMaster("local[2]").setAppName("SparkStreamingDirectKafka")
      val ssc = new StreamingContext(conf,Seconds(2))
      ssc.checkpoint("hdfs://bd1807/flume-kafka-direct")

     /**
       * 读取kafka的数据
     */
     val kafkaParams = Map[String,String]("metadata.broker.list" -> "qyl01:9092,qyl02:9092,qyl03:9092","auto.offset.reset" -> "smallest")
     val topics = Set("flume-kafka-sparkStreaming")
     val kafkaDStream: DStream[String] = KafkaUtils.createDirectStream
              [String, String, StringDecoder, StringDecoder](ssc,kafkaParams,topics).map(_._2)

     /**
       *处理数据逻辑
     */
    val resultDStream = kafkaDStream.flatMap(_.split(" ")).map((_, 1))
      .updateStateByKey[Int]((x: Seq[Int], y: Option[Int]) => {
      Some(x.sum + y.getOrElse(0))
    })
    resultDStream.print()


    /**
      * 启动和结束
    */
    ssc.start()
    ssc.awaitTermination()
    ssc.start()







  }

}
