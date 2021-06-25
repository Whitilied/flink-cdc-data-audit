package com.whitilied

case class RowData(
                    gtids: String,
                    kind: String,
                    timestamp: Long,
                    data: Map[String, AnyRef]
                  )
