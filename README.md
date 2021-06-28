### Flink CDC 案例 表数据变更审查记录

*总体思想：*

监听 目标表 数据变更，将数据具体变更记录到 变更表 中。

启动

```shell
flink run -c com.whitilied.DataAuditJobExample /path/to/flink-cdc-assembly-0.1-SNAPSHOT.jar localhost 3306 root "" test-db target-table
```

### 相关项目

[Debezium](https://debezium.io/)

[Flink](http://flink.apache.org/)

[Flink Cdc Connectors](https://github.com/ververica/flink-cdc-connectors)

