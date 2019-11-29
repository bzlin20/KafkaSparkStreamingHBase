package HBaseDao

import java.io.IOException

import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.util.Bytes

import scala.collection.mutable.ListBuffer

/*
* 操作hbase的dao
* */
object ClickCourseCountDao {
    /*
    * hbase中的各参数，参照个人配置
    * */
  val tableName = "course_clickcount"
  val cf = "info"
  val column = "clickcount"


  /*
  * 插入结果数据的方法
  * */
  def save(List:ListBuffer[ClickCoursCount]):Unit = {
       val htable = HBaseUtils.getInstance().getTable(tableName)
       for(clk <- List){
         htable.incrementColumnValue(
           clk.dayCourse.getBytes(),
           cf.getBytes(),
           column.getBytes(),
           clk.clickCount
         )
       }
  }
  /*
  * 取出tableName中column对应的value的数据
  * */
  def count(dayCourse:String):Long = {
    val htable = HBaseUtils.getInstance().getTable(tableName)
    val get = new Get(dayCourse.getBytes())
    val value = htable.get(get).getValue(cf.getBytes(),column.getBytes())
    if(null == value){
      0L
    }else{
      Bytes.toLong(value)
    }
  }




  def main(args: Array[String]): Unit = {
      val listbuffer = new ListBuffer[ClickCoursCount]
     /*
     * 插入数据测试
     * */
      listbuffer.append(ClickCoursCount("20191111_88",1L))
      listbuffer.append(ClickCoursCount("20191111_88",2L))
      listbuffer.append(ClickCoursCount("20191111_88",2L))
      save(listbuffer)

     println(count("20191111_88")+"------"+count("20191111_88"))

  }
}
  /*
  * 点击量的实体类
  * */
case class ClickCoursCount(dayCourse:String,clickCount:Long)
