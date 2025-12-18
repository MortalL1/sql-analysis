package com.example.sqlanalysis.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * SQL执行结果实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SqlResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * SQL类型（SELECT、UPDATE、INSERT、DELETE等）
     */
    private String sqlType;

    /**
     * 影响行数（用于UPDATE、INSERT、DELETE）
     */
    private Integer affectedRows;

    /**
     * 查询结果（用于SELECT）
     */
    private List<Map<String, Object>> data;

    /**
     * 列名列表
     */
    private List<String> columns;

    /**
     * 执行时间（毫秒）
     */
    private Long executionTime;

    /**
     * SQL语句
     */
    private String sql;
}





