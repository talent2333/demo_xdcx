import java.text.SimpleDateFormat
import java.util.{Calendar, Date, Properties}
import java.sql.DriverManager
import java.text.SimpleDateFormat
import java.util.regex.Pattern

import com.alibaba.fastjson.{JSON, JSONObject}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SaveMode}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.types.{BooleanType, IntegerType, LongType, StringType, StructField, StructType}
import org.junit.Test

/**
 * @description 埋点数据到冒泡数据的转换
 * @author xitianyu
 * @date 2021/3/8
 */
object BuriedData2Bubble {

  def getDistanceTime(s1: String, s2: String): Long = {

    val format: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    var second: Long = 0L
    try {
      val preTime: Long = format.parse(s1).getTime
      val lastTime: Long = format.parse(s2).getTime
      second = (lastTime - preTime) / 1000L
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
    second
  }

  /**
   * 时间戳格式 yyyy-MM-dd HH:mm:ss
   *
   * @param t1 时间戳1
   * @param t2 时间戳2
   * @param t3 时间戳3
   * @param t4 时间戳3
   * @param t  测试的时间戳
   * @return 测试是否在范围内
   */
  def timeInRange(t1: String, t2: String, t3: String, t4: String, t: String): Int = {
    //range 1 to 2
    val t1_t: Long = getDistanceTime(t1, t)
    val t_t2: Long = getDistanceTime(t, t2)
    //range 2 to 3
    val t2_t: Long = getDistanceTime(t2, t)
    val t_t3: Long = getDistanceTime(t, t3)
    //range 3 to 4
    val t3_t: Long = getDistanceTime(t3, t)
    val t_t4: Long = getDistanceTime(t, t4)
    //test t
    if (t1_t > 0 && t_t2 > 0) 1
    else if (t2_t > 0 && t_t3 > 0) 2
    else if (t3_t > 0 && t_t4 > 0) 3
    else 0
  }

  def getSimpleAppVersion(str: String): String = {

    val pattern = ".*\\..*\\..*\\.".r
    //    val str = "192.168.25.12"
    val str_found: String = (pattern findAllIn str).next()
    str_found.substring(0, str_found.length - 1)
  }

  //  def getTomorrowDate(time: String): String = {
  //
  //    val sdf = new SimpleDateFormat("yyyy-MM-dd")
  //    val date: Date = sdf.parse(time)
  //    val calendar: Calendar = Calendar.getInstance()
  //    calendar.setTime(date)
  //    calendar.add(Calendar.DATE,+1)
  //    return sdf.format(calendar.getTime).toString
  //  }

  /**
   * 自定义字典
   * 01 打开APP   02 打开首页   03 输入框(手动埋点)    04 冒泡成功   05 其他-手动    06 其他-自动
   *
   * @param args
   */
  def main(args: Array[String]): Unit = {

    val pt = args(0)
    val today = args(1)
    //    val tommorrow = getTomorrowDate(today)
    val conf = new SparkConf().setAppName("ExtractionFromBuriedData")
    val sc = new SparkContext(conf)
    val hc = new HiveContext(sc)

    val rdd: RDD[Row] = hc.sql(
      """
        |SELECT timestamp,eventname,message
        |FROM hdfs.data_sampling
        |WHERE pt = '$pt'
        |AND appid IN ('com.saicmobility.user', 'iOS_Passenger')
        |AND eventname IN ('AppInfo','PageEvent','DataStatistics')
        |AND timestamp > '$today' AND timestamp < date_add('$today',1)
        |""".stripMargin).rdd.filter(!_.isNullAt(0)).map(
      t => {
        //服务器接收时间 yyyy-MM-dd HH:mm:ss
        val timestamp_server: String = t.get(0).toString.trim
        //事件类型名称
        val event_name: String = t.get(1).toString.trim
        //信息json字段
        val message: String = t.get(2).toString
        //json字段解析
        val json: JSONObject = JSON.parseObject(message)
        //每次APP启动的固定ID（不是会话ID）
        val app_info_refer_id = json.getOrDefault("appInfoReferId", "").toString.trim
        //客户端写日志时间
        //        val timestamp_client = json.getOrDefault("timestamp", "0").toString.trim
        //软件版本
        val app_version = json.getOrDefault("appVersion", "").toString.trim
        //数据字典-事件ID 默认00
        var dataId = "00"
        event_name match {
          //打开APP
          case "AppInfo" => {
            dataId = "01"
          }
          //打开首页、冒泡成功
          case "PageEvent" => {
            val page = json.getOrDefault("page", "").toString
            page match {
              //打开首页
              case "页面_下单首页" | "下单首页" | "车生活" | "车首页"
                   | "SaicPMSecondHomeViewController" | "com.saicmobility.placeorder.ui.MainActivity"
              => dataId = "02"
              //              case "页面_列表选址" | "SaicPSelectOrderAddressVC" => dataId = "03"
              //冒泡成功
              case "页面_确认叫车" | "确认叫车" => dataId = "04"
              case _ => dataId = "06"
            }
          }
          //输入栏-手动埋点
          case "DataStatistics" => {
            val data_id = json.getOrDefault("dataId", "").toString
            data_id match {
              //输入栏-起点
              case "732" | "731" | "738" => dataId = "03"
              //输入栏-终点
              case "735" | "736" | "739" => dataId = "03"
              case _ => dataId = "05"
            }
          }
          case _ => dataId = "06"
        }
        //key    APP单次启动固定ID + APP版本号 + dataId
        //value  描述信息和时间戳
        Row.apply(app_info_refer_id, app_version, dataId, timestamp_server)
      }
    )
    val schema: StructType = StructType(
      StructField("app_info_refer_id", StringType)
        :: StructField("app_version", StringType)
        :: StructField("dataId", StringType)
        :: StructField("timestamp_server", StringType)
        //        :: StructField("timestamp_client", LongType)
        :: Nil)

    hc.createDataFrame(rdd, schema).registerTempTable("tmp")

    //    val rdd_final: RDD[Unit] =
    val rdd_final: RDD[Row] = hc.sql(
      """
        |select app_version,app_info_refer_id,dataId,
        |min(timestamp_server) as timestamp_server
        |from
        |(select app_version,app_info_refer_id,dataId,timestamp_server
        |from tmp where dataId in ("01","02","03","04")
        |) as t2
        |group by app_version,app_info_refer_id,dataId
        |""".stripMargin).rdd.map(
      x => {
        val app_version = x.get(0).toString.trim
        val app_info_refer_id = x.get(1).toString.trim
        val dataId = x.get(2).toString.trim
        val timestamp_server = x.get(3).toString.trim
        //        val timestamp_client = x.get(4).toString
        ((app_version, app_info_refer_id), dataId + "@" + timestamp_server)
      })
      .reduceByKey((v1, v2) => v1 + "&&" + v2)
      .map(t => {
        //app_version格式 xxx.xxx.xxx
        val app_version: String = getSimpleAppVersion(t._1._1)
        val app_info_refer_id = t._1._2
        //相同版本 和 APP启动固定ID 的 事件（带时间戳）数组
        val data_array = t._2.split("&&")

        var open_app_time = "1970-01-01 00:00:00"
        var enter_homepage_time = "1970-01-01 00:00:00"
        var focus_inputbox_time = "1970-01-01 00:00:00"
        var bubble_success_time = "1970-01-01 00:00:00"

        var openApp_2_homepage_dataIds: StringBuilder = new StringBuilder
        var homepage_2_inputbox_dataIds: StringBuilder = new StringBuilder
        var inputbox_2_bubble_dataIds: StringBuilder = new StringBuilder

        //每条事件数据，确定四个阶段的时间
        for (dataId_timestamp <- data_array) {
          val item: Array[String] = dataId_timestamp.split("@")
          val dataId = item(0)
          val timestamp_server = item(1)
          dataId match {
            case "01" => open_app_time = timestamp_server
            case "02" => enter_homepage_time = timestamp_server
            case "03" => focus_inputbox_time = timestamp_server
            case "04" => bubble_success_time = timestamp_server
            case _ =>
          }
        }

        //打开==》首页 ==》地址栏==》冒泡成功 的时间戳间隔 单位 秒
        var time_diff1: Long = getDistanceTime(open_app_time, enter_homepage_time)
        var time_diff2: Long = getDistanceTime(enter_homepage_time, focus_inputbox_time)
        var time_diff3: Long = getDistanceTime(focus_inputbox_time, bubble_success_time)

        for (dataId_timestamp <- data_array) {
          val item: Array[String] = dataId_timestamp.split("@")
          val dataId = item(0)
          val timestamp_server = item(1)
          val result: Int = timeInRange(open_app_time, enter_homepage_time, focus_inputbox_time,
            bubble_success_time, timestamp_server)
          result match {
            case 1 => openApp_2_homepage_dataIds.append(dataId).append(",")
            case 2 => homepage_2_inputbox_dataIds.append(dataId).append(",")
            case 3 => inputbox_2_bubble_dataIds.append(dataId).append(",")
            case _ =>
          }
        }
        //去除最后一个逗号
        openApp_2_homepage_dataIds.deleteCharAt(openApp_2_homepage_dataIds.lastIndexOf(","))
        homepage_2_inputbox_dataIds.deleteCharAt(homepage_2_inputbox_dataIds.lastIndexOf(","))
        inputbox_2_bubble_dataIds.deleteCharAt(inputbox_2_bubble_dataIds.lastIndexOf(","))

        //输出结构 版本-每次打开APP固定ID,四个固定时间戳，3个时间间隔，3个间隔内的行为ID
        Row(app_version, app_info_refer_id,
          open_app_time, enter_homepage_time, focus_inputbox_time, bubble_success_time,
          time_diff1, time_diff2, time_diff3,
          openApp_2_homepage_dataIds.toString, homepage_2_inputbox_dataIds.toString, inputbox_2_bubble_dataIds.toString)
      }
      )

    val schema_final = StructType(
      StructField("app_version", StringType) ::
        StructField("app_info_refer_id", StringType) ::
        StructField("open_app_time", StringType) ::
        StructField("enter_homepage_time", StringType) ::
        StructField("focus_inputbox_time", LongType) ::
        StructField("bubble_success_time", StringType) ::
        StructField("time_diff1", StringType) ::
        StructField("time_diff2", LongType) ::
        StructField("time_diff3", StringType) ::
        StructField("openApp_2_homepage_dataIds", StringType) ::
        StructField("homepage_2_inputbox_dataIds", LongType) ::
        StructField("inputbox_2_bubble_dataIds", StringType) :: Nil
    )

    //创建临时表
    hc.createDataFrame(rdd_final, schema_final).registerTempTable("myBubble")
    //清洗完的数据导入目标表
    hc.sql("insert overwrite table  rpt.bubble_transform partition(pt=$pt)  select * from myBubble")

    sc.stop

  }

}
