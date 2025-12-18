package com.example.sqlanalysis.service;

import com.example.sqlanalysis.entity.SqlRequest;
import com.example.sqlanalysis.entity.SqlResult;
import com.example.sqlanalysis.enums.DataSourceType;
import com.example.sqlanalysis.executor.QueryExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL执行服务类（多数据源支持）
 */
@Slf4j
@Service
public class SqlExecutionService {

    @Autowired
    private List<QueryExecutor> executors;

    /**
     * 执行查询（根据数据源类型选择执行器）
     */
    public SqlResult executeSql(SqlRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求对象不能为空");
        }

        // 获取数据源类型，默认为mysql
        String dataSourceType = request.getDataSourceType();
        if (dataSourceType == null || dataSourceType.trim().isEmpty()) {
            dataSourceType = DataSourceType.MYSQL.getCode();
        }

        log.info("数据源类型: {}, 查询语句: {}", dataSourceType, request.getSql());

        // 查找对应的执行器
        QueryExecutor executor = findExecutor(dataSourceType);
        if (executor == null) {
            throw new IllegalArgumentException("不支持的数据源类型: " + dataSourceType);
        }

        // 执行查询
        try {
            return executor.execute(request);
        } catch (Exception e) {
            log.error("执行失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 查找对应的执行器
     */
    private QueryExecutor findExecutor(String dataSourceType) {
        for (QueryExecutor executor : executors) {
            if (executor.support(dataSourceType)) {
                return executor;
            }
        }
        return null;
    }

    /**
     * 批量执行SQL
     */
    public List<SqlResult> executeBatchSql(List<SqlRequest> requests) {
        List<SqlResult> results = new ArrayList<>();
        for (SqlRequest request : requests) {
            try {
                SqlResult result = executeSql(request);
                results.add(result);
            } catch (Exception e) {
                SqlResult errorResult = new SqlResult();
                errorResult.setSql(request.getSql());
                errorResult.setSqlType("ERROR");
                errorResult.setExecutionTime(0L);
                results.add(errorResult);
                log.error("批量执行SQL失败，SQL: {}, 错误: {}", request.getSql(), e.getMessage());
            }
        }
        return results;
    }
}



