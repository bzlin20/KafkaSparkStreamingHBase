package com.bonc.qyl.Spark

import HBaseDao.{ClickCoursCount, ClickCourseCountDao}
import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.mutable.ListBuffer

/*
*  flume + kafka +SparkStreaming +HBase
* */
object ProjectStreaming {
  def main(args: Array[String]): Unit = {
    /*
    *    实际项目中 应该是传入参数，这里不做演示了
    * if(args.length != 2){
      System.err.println("Usage ProjectStreaming: <brokers> <topics>")
      System.exit(1)
    }
   */

    /*
    * 建立连接
    * */
    System.setProperty("HADOOP_USER_NAME","qyl")
    val conf = new SparkConf().setMaster("local[2]").setAppName("ProjectStreaming")
    val ssc = new StreamingContext(conf,Seconds(5))
    ssc.checkpoint("hdfs:///flume-kafka-direct")

    /*
    *  读取kafka中的数据
    * */
    val kafkaParams = Map[String,String]("metadata.broker.list" -> "qyl01:9092,qyl02:9092,qyl03:9092","auto.offset.reset" -> "smallest")
    val topics = Set("flume-kafka-sparkStreaming-HBase1")
    val kafkaDStream: DStream[String] = KafkaUtils.createDirectStream
      [String, String, StringDecoder, StringDecoder](ssc,kafkaParams,topics).map(_._2)

    /*
  * 数据过滤
  * 数据格式
  * 132.168.89.224    2018-07-13 05:53:02 "GET /class/145.html HTTP/1.1"  200 https://search.yahoo.com/search?p=Flink实战
  * */
    val   cleanData : DStream[ClickLog]  = kafkaDStream.map { x =>
      val strArr = x.split("\t")
        val ip = strArr(0)
        val time = strArr(1).substring(0,10).trim()
        val refer = strArr(2).split(" ")(1)
        val status = strArr(3).toInt
        val searchArr = strArr(4).replaceAll("//", "/").split("/")
        var searchUrl = ""
        if (searchArr.length > 2) {
          searchUrl = searchArr(1)
        } else {
          searchUrl = searchArr(0)
        }
        (ip, time, refer, status, searchUrl)
    }.filter(_._3.startsWith("/class")).map { x =>
      // 145.html
      val referStr = x._3.split("/")(2)
      val refer = referStr.substring(0, referStr.lastIndexOf("."))
      ClickLog(x._1, x._2, refer, x._4, x._5)
    }
      /*
    * 需求：统计到今天为止，的访问量
    */

    cleanData.map(x =>(x.time +"_"+x.refer,1)).reduceByKey(_+_).foreachRDD{rdd =>{
      rdd.foreachPartition{rddPartition =>
        val list = new ListBuffer[ClickCoursCount]
        rddPartition.foreach{ pair =>
          list.append(ClickCoursCount(pair._1,pair._2))
        }
        /*
        * 写入数据到HBase
        * */
        ClickCourseCountDao.save(list)
      }
    }}


    ssc.start()
    ssc.awaitTermination()

  }
}

case class ClickLog(ip:String,time:String,refer:String,status:Int,searchUrl:String)