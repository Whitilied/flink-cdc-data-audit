package com.whitilied

import com.alibaba.ververica.cdc.debezium.DebeziumDeserializationSchema
import io.debezium.data.Envelope
import io.debezium.data.Envelope.Operation
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.types.RowKind
import org.apache.flink.util.Collector
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.source.SourceRecord

import scala.collection.JavaConversions._

class RowDataDeserializationSchema extends DebeziumDeserializationSchema[RowData] {

  override def deserialize(sourceRecord: SourceRecord, collector: Collector[RowData]): Unit = {
    val gtids = sourceRecord.sourceOffset().get(DebeziumFiledConstant.GTIDS).toString
    val operation: Envelope.Operation = Envelope.operationFor(sourceRecord);
    val struct = sourceRecord.value().asInstanceOf[Struct]

    def getMap(name: String) = {
      val s = struct.getStruct(name)
      if (s == null) {
        Map.empty[String, AnyRef]
      } else {
        s.schema().fields().map(f => (f.name(), s.get(f.name()))).toMap
      }
    }

    val before = getMap(DebeziumFiledConstant.BEFORE)
    val after = getMap(DebeziumFiledConstant.AFTER)

    val timestamp = sourceRecord.sourceOffset().get(DebeziumFiledConstant.TIMESTAMP).asInstanceOf[Long]
    operation match {
      case Operation.UPDATE =>
        collector.collect(RowData(gtids, RowKind.UPDATE_BEFORE.shortString(), timestamp, before))
        collector.collect(RowData(gtids, RowKind.UPDATE_AFTER.shortString(), timestamp, after))
      case Operation.DELETE =>
        collector.collect(RowData(gtids, RowKind.DELETE.shortString(), timestamp, before))
    }
  }

  override def getProducedType: TypeInformation[RowData] = {
    TypeInformation.of(classOf[RowData])
  }

}
