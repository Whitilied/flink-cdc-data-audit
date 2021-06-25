package com.whitilied.util

import java.sql.{DriverManager, PreparedStatement, Types}
import scala.collection.mutable.ListBuffer

object JdbcUtil {

  def showCreateTable(dbUrl: String, user: String, password: String, targetTale: String) = {
    val conn = DriverManager.getConnection(dbUrl, user, password)
    val ps = conn.createStatement()
    val rs = ps.executeQuery(s"show create table `${targetTale}`")
    rs.next()
    val sDml = rs.getString(2)
    val incrColumn =
      """
        |  `_id` varchar(60) NOT NULL,
        |  `_kind` varchar(10) NOT NULL,
        |  `_ts` int NOT NULL,
        |""".stripMargin
    val dml = sDml.replaceFirst("\n", incrColumn)
      .replaceFirst("AUTO_INCREMENT", "")
    println(dml)
    rs.close()
    ps.close()
    conn.close()
    dml
  }

  def existsChangeTable(dbUrl: String, user: String, password: String, targetTale: String): Boolean = {
    val conn = DriverManager.getConnection(dbUrl, user, password)
    val ps = conn.createStatement()
    val rs = ps.executeQuery("show tables like \"" + targetTale + "_change\"")
    var exists = false
    if (rs.next()) {
      exists = true
    }
    rs.close()
    ps.close()
    conn.close()
    exists
  }

  def createChangeTable(dbUrl: String, user: String, password: String, targetTale: String, dml: String) = {
    val conn = DriverManager.getConnection(dbUrl, user, password)
    val ps = conn.createStatement()
    ps.execute(dml.replaceFirst(targetTale, targetTale + "_change"))
    ps.close()
    conn.close()
  }

  def showTableColumns(dbUrl: String, user: String, password: String, targetTale: String): Seq[String] = {
    val conn = DriverManager.getConnection(dbUrl, user, password)
    val ps = conn.createStatement()
    val rs = ps.executeQuery(s"show columns from `${targetTale}`")
    val buf = ListBuffer[String]()
    while (rs.next()) {
      val columnName = rs.getString(1)
      buf.append(columnName)
      println(s"column name:${columnName}")
    }
    buf.foreach(println)
    rs.close()
    ps.close()
    conn.close()
    buf.toSeq
  }

  def editPrimaryKey(dbUrl: String, user: String, password: String, targetTale: String, oldPk: String, newPk: String) = {
    val conn = DriverManager.getConnection(dbUrl, user, password)
    val ps = conn.createStatement()
    ps.execute(s"alter table `${targetTale}` drop primary key")
    ps.execute(s"alter table `${targetTale}` add index `${oldPk}_idx` (${oldPk})")
    ps.execute(s"alter table `${targetTale}` add primary key `${newPk}` (${newPk})")
    ps.close()
    conn.close()
  }

  def dataBind(idx: Int, field: Option[Any], ps: PreparedStatement) = {
    field match {
      case Some(v: String) =>
        ps.setString(idx + 1, v)
      case Some(v: Int) =>
        ps.setInt(idx + 1, v)
      case Some(v: Long) =>
        ps.setLong(idx + 1, v)
      case Some(v: Boolean) =>
        ps.setBoolean(idx + 1, v)
      case Some(v: Short) =>
        ps.setShort(idx + 1, v)
      case Some(v: Float) =>
        ps.setFloat(idx + 1, v)
      case Some(v: Byte) =>
        ps.setByte(idx + 1, v)
      case Some(v: Array[Byte]) =>
        ps.setBytes(idx + 1, v)
      case Some(v: Double) =>
        ps.setDouble(idx + 1, v)
      case Some(v: java.sql.Time) =>
        ps.setTime(idx + 1, v)
      case Some(v: java.sql.Timestamp) =>
        ps.setTimestamp(idx + 1, v)
      case Some(v: java.sql.Date) =>
        ps.setDate(idx + 1, v)
      case Some(v: java.math.BigDecimal) =>
        ps.setBigDecimal(idx + 1, v)
      case Some(null) =>
        ps.setNull(idx + 1, Types.NULL)
    }
  }

}
