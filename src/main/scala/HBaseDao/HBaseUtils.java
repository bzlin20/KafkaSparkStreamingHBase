package HBaseDao;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import java.io.IOException;

/*
*
*   操作Hbase的工具类
* */
public class HBaseUtils {
    HBaseAdmin admin = null;
    Configuration configuration = null;
    /*
     *  私有化构造器
     * */
    private HBaseUtils() {
        configuration = new Configuration();
        configuration.set("hbase.zookeeper.quorum","qyl01,qyl02,qyl03");
        configuration.set("hbase.rootdir","hdfs:///hbase");
        try{
            admin = new HBaseAdmin(configuration);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
    private static HBaseUtils instance = null;

    public static HBaseUtils getInstance() {
        if (null == instance){
            instance = new HBaseUtils();
        }
        return instance;
    }
    /*
    *   根据表名获取HBase实例
    *   @param tableName
    *   @return
    * */
    public HTable getTable(String tableName){
        HTable table  = null ;
        try {
            table  = new HTable(configuration, tableName);
        }catch (IOException e){
            e.printStackTrace();
        }
        return table;
    }

    /**
     * 添加一条记录到HBase表
     * @param tableName 表名
     * @param rowkey rowkey
     * @param cf columnFamily
     * @param column 列
     * @param value 写入的值
     */
    public void put(String tableName,String rowkey,String cf,String column,String value){
        HTable table = getTable(tableName);
        Put put = new Put(rowkey.getBytes());
        put.add(cf.getBytes(),column.getBytes(),value.getBytes());

        try{
            table.put(put);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    /*
    * 插入一条数据到HBase中进行操作
    * */
/*
    public static void main(String[] args) {
        String tableName = "course_clickcount";
        String rowkey = "20191111_188";
        String cf = "info";
        String column = "click_count";
        String value = "2";
        HBaseUtils.getInstance().put(tableName,rowkey,cf,column,value);

      }
*/
}
