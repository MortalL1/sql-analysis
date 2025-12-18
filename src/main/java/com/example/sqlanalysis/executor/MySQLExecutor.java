package com.example.sqlanalysis.executor;

import com.example.sqlanalysis.entity.SqlRequest;
import com.example.sqlanalysis.entity.SqlResult;
import com.example.sqlanalysis.enums.DataSourceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MySQL查询执行器
 */
@Slf4j
@Component
public class MySQLExecutor implements QueryExecutor {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public SqlResult execute(SqlRequest request) {
        String sql = request.getSql();
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL语句不能为空");
        }

        sql = sql.trim();
        log.info("开始执行SQL: {}", sql);

        long startTime = System.currentTimeMillis();
        SqlResult result = new SqlResult();
        result.setSql(sql);

        try {
            // 判断SQL类型
            String sqlType = getSqlType(sql);
            result.setSqlType(sqlType);

            if ("SELECT".equalsIgnoreCase(sqlType)) {
                // 执行查询
                executeQuery(sql, result, request.getTimeout());
            } else {
                // 执行更新（INSERT、UPDATE、DELETE等）
                executeUpdate(sql, result, request.getTimeout());
            }

            long endTime = System.currentTimeMillis();
            result.setExecutionTime(endTime - startTime);
            log.info("SQL执行成功，耗时: {}ms", result.getExecutionTime());

            return result;
        } catch (Exception e) {
            log.error("SQL执行失败: {}", e.getMessage(), e);
            throw new RuntimeException("SQL执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行查询SQL
     */
    private void executeQuery(String sql, SqlResult result, Integer timeout) {
        jdbcTemplate.query(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            if (timeout != null && timeout > 0) {
                ps.setQueryTimeout(timeout);
            }
            return ps;
        }, rs -> {
            try {
                // 获取列信息
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                List<String> columns = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    columns.add(metaData.getColumnLabel(i));
                }
                result.setColumns(columns);

                // 获取数据（使用LinkedHashMap保持列顺序）
                List<Map<String, Object>> data = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnLabel(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    data.add(row);
                }
                result.setData(data);
                result.setAffectedRows(data.size());
            } catch (SQLException e) {
                throw new RuntimeException("处理查询结果失败", e);
            }
        });
    }

    /**
     * 执行更新SQL（INSERT、UPDATE、DELETE等）
     */
    private void executeUpdate(String sql, SqlResult result, Integer timeout) {
        int affectedRows = jdbcTemplate.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            if (timeout != null && timeout > 0) {
                ps.setQueryTimeout(timeout);
            }
            return ps;
        }, (PreparedStatement ps) -> {
            return ps.executeUpdate();
        });
        
        result.setAffectedRows(affectedRows);
        result.setData(new ArrayList<>());
        result.setColumns(new ArrayList<>());
    }

    /**
     * 获取SQL类型
     */
    private String getSqlType(String sql) {
        String upperSql = sql.toUpperCase().trim();
        
        if (upperSql.startsWith("SELECT")) {
            return "SELECT";
        } else if (upperSql.startsWith("INSERT")) {
            return "INSERT";
        } else if (upperSql.startsWith("UPDATE")) {
            return "UPDATE";
        } else if (upperSql.startsWith("DELETE")) {
            return "DELETE";
        } else if (upperSql.startsWith("CREATE")) {
            return "CREATE";
        } else if (upperSql.startsWith("DROP")) {
            return "DROP";
        } else if (upperSql.startsWith("ALTER")) {
            return "ALTER";
        } else if (upperSql.startsWith("TRUNCATE")) {
            return "TRUNCATE";
        } else {
            return "OTHER";
        }
    }

    @Override
    public boolean support(String dataSourceType) {
        return DataSourceType.MYSQL.getCode().equalsIgnoreCase(dataSourceType);
    }
}

