package com.saic.offline

import java.text.SimpleDateFormat

import com.alibaba.fastjson.{JSON, JSONObject}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.types.{LongType, StringType, StructField, StructType}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable
import scala.collection.mutable.{ListBuffer, TreeSet}

/**
 * @description 埋点数据到冒泡数据的转换
 * @author xitianyu
 * @date 2021/3/8
 */
object BuriedData2Bubble {

  def getDistanceTime(s1: String, s2: String): Long = {

    val format: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    var second: Long = 0L
    var millisecond: Long = 0L
    try {
      val preTime: Long = format.parse(s1).getTime
      val lastTime: Long = format.parse(s2).getTime
      millisecond = lastTime - preTime
      if (millisecond < 0) return 0L
      second = millisecond / 1000L
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

  /**
   * 自定义字典 data_status
   * 01 打开APP   02 打开首页   03 输入框(手动埋点)    04 冒泡成功   05 其他-手动空值    06 其他-自动    07 异常数据
   *
   * @param args
   */
  def main(args: Array[String]): Unit = {

    //pt时间格式: yyyyMMdd （hive分区字段值）
    val pt = args(0)
    //formatted_date时间格式: yyyy-MM-dd
    val formatted_date = args(1)

    val conf = new SparkConf().setAppName("ExtractionFromBuriedData")
    val sc = new SparkContext(conf)
    val hc = new HiveContext(sc)

    //版本固定
    val app_version_static = "3.3.3.140293"

    val rdd: RDD[Row] = hc.sql(
      s"""
         |SELECT timestamp,eventname,message
         |FROM hdfs.data_sampling
         |WHERE pt = '$pt'
         |AND appid IN ('com.saicmobility.user', 'iOS_Passenger')
         |AND eventname IN ('AppInfo','PageEvent','DataStatistics')
         |AND timestamp > '$formatted_date' AND timestamp < date_add('$formatted_date',1)
         |""".stripMargin).rdd.filter(t => {
      val json: JSONObject = JSON.parseObject(t.get(2).toString)
      val app_version = json.getOrDefault("appVersion", "").toString.trim
      app_version == app_version_static
    }).map(
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
        //软件版本
        val app_version = json.getOrDefault("appVersion", "").toString.trim
        //数据字典-事件状态 默认-1
        var data_status = "-1"
        var data_id = "-1"
        event_name match {
          //打开APP
          case "AppInfo" => {
            data_status = "01"
            data_id = "t1"
          }
          //打开首页、冒泡成功
          case "PageEvent" => {
            val page = json.getOrDefault("page", "").toString
            page match {
              //打开首页
              case "页面_下单首页" | "下单首页" | "车生活" | "车首页"
                   | "SaicPMSecondHomeViewController" | "com.saicmobility.placeorder.ui.MainActivity" =>
                data_status = "02"
                data_id = "t2"
              //              case "页面_列表选址" | "SaicPSelectOrderAddressVC" => dataId = "03"
              //冒泡成功
              case "页面_确认叫车" | "确认叫车" =>
                data_status = "04"
                data_id = "t4"
              //其他-自动
              case _ => data_status = "06"
            }
          }
          //输入栏-手动埋点
          case "DataStatistics" => {
            data_id = json.getOrDefault("dataId", "").toString
            data_id match {
              //输入栏-起点
              case "732" | "731" | "738" =>
                data_status = "03"
                data_id = "t3"
              //输入栏-终点
              case "735" | "736" | "739" =>
                data_status = "03"
                data_id = "t3"
              //异常空值
              case "" => data_status = "07"
              //其他-手动
              case _ => data_status = "05"
            }
          }
          case _ => data_status = "06"
        }
        Row.apply(app_version, app_info_refer_id, data_status, data_id, timestamp_server)
      }
    )
    val schema: StructType = StructType(
      StructField("app_version", StringType)
        :: StructField("app_info_refer_id", StringType)
        :: StructField("data_status", StringType)
        :: StructField("data_id", StringType)
        :: StructField("timestamp_server", StringType)
        :: Nil)

    hc.createDataFrame(rdd, schema).registerTempTable("tmp")

    val rdd_final: RDD[Row] = hc.sql(
      s"""
         |select app_version,app_info_refer_id,data_id,timestamp_server
         |from tmp
         |where data_status in ('01','02','03','04','05')
         |group by app_version,app_info_refer_id,data_id,timestamp_server
         |""".stripMargin).rdd.map(
      x => {
        val app_version = x.get(0).toString.trim
        val app_info_refer_id = x.get(1).toString.trim
        //data_status是数据分类的状态码，data_id是具体数据行为id
        val dataId = x.get(2).toString.trim
        val timestamp_server = x.get(3).toString.trim
        ((app_version, app_info_refer_id), timestamp_server + "@" + dataId)
      })
      .reduceByKey((v1, v2) => v1 + "&&" + v2)
      .map(t => {
        //app_version格式 xxx.xxx.xxx
        val app_version: String = getSimpleAppVersion(t._1._1)
        val app_info_refer_id = t._1._2
        //相同版本 和 APP启动固定ID 的 事件（带时间戳）数组
        val data_array = t._2.split("&&")

        val default_time = "1970-01-01 00:00:00"
        var open_app_time = default_time
        var enter_homepage_time = default_time
        var focus_inputbox_time = default_time
        var bubble_success_time = default_time
        val open_app_time_list: ListBuffer[String] = mutable.ListBuffer.empty[String]
        val enter_homepage_time_list: ListBuffer[String] = mutable.ListBuffer.empty[String]
        val focus_inputbox_time_list: ListBuffer[String] = mutable.ListBuffer.empty[String]
        val bubble_success_time_list: ListBuffer[String] = mutable.ListBuffer.empty[String]

        val openApp_2_homepage_dataIds_list = mutable.ListBuffer.empty[String]
        val homepage_2_inputbox_dataIds_list = mutable.ListBuffer.empty[String]
        val inputbox_2_bubble_dataIds_list = mutable.ListBuffer.empty[String]

        //每条事件数据，确定四个阶段的时间
        for (timestamp_dataId <- data_array) {
          val item: Array[String] = timestamp_dataId.split("@")
          val dataId = item(1)
          dataId match {
            case "t1" => open_app_time_list += timestamp_dataId
            case "t2" => enter_homepage_time_list += timestamp_dataId
            case "t3" => focus_inputbox_time_list += timestamp_dataId
            case "t4" => bubble_success_time_list += timestamp_dataId
            case _ =>
          }
        }
        //打开APP取最早出现的时间，
        // 进入首页、输入地址栏、冒泡成功取最晚出现的时间
        if (open_app_time_list.nonEmpty)
          open_app_time = open_app_time_list.sortBy(t => t).head.split("@")(0)
        if (enter_homepage_time_list.nonEmpty)
          enter_homepage_time = enter_homepage_time_list.sortBy(t => t).last.split("@")(0)
        if (focus_inputbox_time_list.nonEmpty)
          focus_inputbox_time = focus_inputbox_time_list.sortBy(t => t).last.split("@")(0)
        if (bubble_success_time_list.nonEmpty)
          bubble_success_time = bubble_success_time_list.sortBy(t => t).last.split("@")(0)

        //打开==》首页 ==》地址栏==》冒泡成功 的时间戳间隔 单位-秒
        var time_diff1: String = getDistanceTime(open_app_time, enter_homepage_time).toString
        var time_diff2: String = getDistanceTime(enter_homepage_time, focus_inputbox_time).toString
        var time_diff3: String = getDistanceTime(focus_inputbox_time, bubble_success_time).toString

        for (timestamp_dataId <- data_array) {
          val item: Array[String] = timestamp_dataId.split("@")
          val timestamp = item(0)
          val timestmap_dataId = item(0) + "&&" + item(1)
          val result: Int = timeInRange(open_app_time, enter_homepage_time, focus_inputbox_time,
            bubble_success_time, timestamp)
          result match {
            case 1 => openApp_2_homepage_dataIds_list += timestmap_dataId
            case 2 => homepage_2_inputbox_dataIds_list += timestmap_dataId
            case 3 => inputbox_2_bubble_dataIds_list += timestmap_dataId
            case _ =>
          }
        }
        //新增功能: 按时间戳顺序排序不同时段内的数据
        //字符串格式-逗号分割-只保留dataId
        val openApp_2_homepage_dataIds = openApp_2_homepage_dataIds_list
          .sortBy(t => t)
          .map(t => t.split("&&")(1))
          .mkString(",")
        val homepage_2_inputbox_dataIds = homepage_2_inputbox_dataIds_list
          .sortBy(t => t)
          .map(t => t.split("&&")(1))
          .mkString(",")
        val inputbox_2_bubble_dataIds = inputbox_2_bubble_dataIds_list
          .sortBy(t => t)
          .map(t => t.split("&&")(1))
          .mkString(",")

        //设置四个时间戳 数据未取到时=0
        if (default_time == open_app_time) open_app_time = "0"
        if (default_time == enter_homepage_time) enter_homepage_time = "0"
        if (default_time == focus_inputbox_time) focus_inputbox_time = "0"
        if (default_time == bubble_success_time) bubble_success_time = "0"

        //输出结构 版本-每次打开APP固定ID,四个固定时间戳，3个时间间隔，3个间隔内的行为ID
        //新增功能 按照时间排序dataId
        Row(
          app_version,
          app_info_refer_id,
          open_app_time,
          enter_homepage_time,
          focus_inputbox_time,
          bubble_success_time,
          time_diff1, time_diff2, time_diff3,
          openApp_2_homepage_dataIds,
          homepage_2_inputbox_dataIds,
          inputbox_2_bubble_dataIds)
      })

    val schema_final = StructType(
      StructField("app_version", StringType) ::
        StructField("app_info_refer_id", StringType) ::
        StructField("open_app_time", StringType) ::
        StructField("enter_homepage_time", StringType) ::
        StructField("focus_inputbox_time", StringType) ::
        StructField("bubble_success_time", StringType) ::
        StructField("time_diff1", StringType) ::
        StructField("time_diff2", StringType) ::
        StructField("time_diff3", StringType) ::
        StructField("openApp_2_homepage_dataIds", StringType) ::
        StructField("homepage_2_inputbox_dataIds", StringType) ::
        StructField("inputbox_2_bubble_dataIds", StringType) :: Nil
    )

    //创建临时表
    hc.createDataFrame(rdd_final, schema_final).registerTempTable("myBubble")
    //清洗完的数据导入目标表
    hc.sql(s"insert overwrite table test.bubble_info partition(pt=$pt) select * from myBubble")

    sc.stop

  }

}
