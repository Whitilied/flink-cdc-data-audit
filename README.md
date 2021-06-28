### Flink CDC 案例 表数据变更审查记录

*总体思想：*

监听 目标表 数据变更，将数据具体变更记录到 自动创建的变更表 中，可以方便 join 前后两张表进行数据审计工作。

启动

* IDEA 启动

启动配置 选择 `Include dependencies with "Provided" scope`，并添加程序参数 ` localhost 3306 root pwd test-db target-table`


* flink 提交

```shell
flink run -c com.whitilied.DataAuditJobExample /path/to/flink-cdc-assembly-0.1-SNAPSHOT.jar localhost 3306 root pwd test-db target-table
```

### 相关项目

[Debezium](https://debezium.io/)

[Flink](http://flink.apache.org/)

[Flink Cdc Connectors](https://github.com/ververica/flink-cdc-connectors)

