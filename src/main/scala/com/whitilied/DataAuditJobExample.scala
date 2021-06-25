package com.whitilied

import com.alibaba.ververica.cdc.connectors.mysql.MySQLSource
import com.alibaba.ververica.cdc.connectors.mysql.table.StartupOptions
import com.alibaba.ververica.cdc.debezium.DebeziumSourceFunction
import com.whitilied.util.JdbcUtil
import com.whitilied.util.JdbcUtil._
import org.apache.flink.connector.jdbc.{JdbcConnectionOptions, JdbcExecutionOptions, JdbcSink, JdbcStatementBuilder}
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}

import java.sql.PreparedStatement

object DataAuditJobExample {

  def main(args: Array[String]): Unit = {
    if (args.length != 6) {
      System.err.println("USAGE:\nDataAuditJobExample <hostname> <port> <user> <password> <db> <table>")
      return
    }

    println(s"Paramenter: ${args.mkString(",")}")

    val dbHost = args(0)
    val dbPort = args(1).toInt
    val dbUser = args(2)
    val dbPassword = args(3)
    val dbName = args(4)
    val dbTargetTable = args(5)

    val dbUrl = s"jdbc:mysql://$dbHost:$dbPort/$dbName"

    if (!existsChangeTable(dbUrl, dbUser, dbPassword, dbTargetTable)) {
      println("change table not exists, init it")
      val dml = showCreateTable(dbUrl, dbUser, dbPassword, dbTargetTable)
      createChangeTable(dbUrl, dbUser, dbPassword, dbTargetTable, dml)
      editPrimaryKey(dbUrl, dbUser, dbPassword, dbTargetTable + "_change", "id", "_id")
      showTableColumns(dbUrl, dbUser, dbPassword, dbTargetTable)
    }

    val sourceFunction: DebeziumSourceFunction[RowData] = MySQLSource.builder[RowData]()
      .hostname(dbHost)
      .port(dbPort)
      .databaseList(dbName) // monitor all tables under inventory database
      .tableList(dbName + "." + dbTargetTable)
      .username(dbUser)
      .password(dbPassword)
      .deserializer(new RowDataDeserializationSchema()) // converts SourceRecord to String
      .startupOptions(StartupOptions.latest())
      .build()

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val baseFields = Seq("_id", "_kind", "_ts")
    val busFields = JdbcUtil.showTableColumns(dbUrl, dbUser, dbPassword, dbTargetTable)
    val totalFields = (baseFields ++ busFields)
      .filter(_ => true) // 过滤条件？

    val sqlStr =
      s"""insert into $dbName.${dbTargetTable}_change
         |(${totalFields.mkString(",")})
         |values
         |(${Array.fill(totalFields.length)("?").mkString(",")})""".stripMargin

    val statementBuilder: JdbcStatementBuilder[RowData] = new JdbcStatementBuilder[RowData] {
      override def accept(ps: PreparedStatement, flatData: RowData): Unit = {
        val id = flatData.gtids + ":" + flatData.kind
        val map = Map("_id" -> id,
          "_kind" -> flatData.kind,
          "_ts" -> flatData.timestamp) ++ flatData.data

        if (map.size != (baseFields.length + busFields.length)) {
          throw new RuntimeException("data size illegal")
        }

        totalFields.zipWithIndex.foreach {
          case (field, idx) =>
            try {
              JdbcUtil.dataBind(idx, map.get(field), ps)
            } catch {
              case e: Throwable =>
                e.printStackTrace()
            }
        }
      }
    }

    val sink: SinkFunction[RowData] = JdbcSink.sink(
      sqlStr,
      statementBuilder,
      JdbcExecutionOptions.builder()
        .withBatchSize(100)
        .withBatchIntervalMs(200)
        .withMaxRetries(5)
        .build(),
      new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
        .withUrl(dbUrl)
        .withDriverName("com.mysql.cj.jdbc.Driver")
        .withUsername(dbUser)
        .withPassword(dbPassword)
        .build())

    env
      .addSource(sourceFunction)
      .map(s => s)
      .addSink(sink)

    env.execute()
  }

}
