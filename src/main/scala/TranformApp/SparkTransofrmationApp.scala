package TranformApp

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * SparkStreaming实现黑名单过滤
*/
object SparkTransofrmationApp {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[2]").setAppName("SparkTransofrmationApp")
    val ssc = new StreamingContext(conf,Seconds(5))
    val lines = ssc.socketTextStream("qyl01",66666)
  /*
  *  构建黑名单
  * */
    val blacks = List("zs","ls")
    val blackRDD = ssc.sparkContext.parallelize(blacks).map(x=>(x,true))

    val clicklog = lines.map(x => (x.split(",")(1), x)).transform(rdd => {
      rdd.leftOuterJoin(blackRDD).filter(x => x._2._2.getOrElse(false) != true)
        .map(x => x._2._1)
    }
    )
    clicklog.print()

    ssc.start()
    ssc.awaitTermination()
  }
}
