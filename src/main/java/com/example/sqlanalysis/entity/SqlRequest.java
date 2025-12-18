package com.example.sqlanalysis.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * SQL请求实体类
 */
@Data
public class SqlRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据源类型：mysql, mongodb, redis
     * 默认为mysql
     */
    private String dataSourceType = "mysql";

    /**
     * SQL语句/查询命令
     * - MySQL: SQL语句，如 "SELECT * FROM users"
     * - MongoDB: JSON格式的查询命令，如 {"collection": "users", "operation": "find", "query": {"age": {"$gt": 25}}}
     * - Redis: Redis命令，如 {"command": "GET", "key": "user:1"}
     */
    private String sql;

    /**
     * 查询超时时间（秒），默认30秒
     */
    private Integer timeout = 30;
}



