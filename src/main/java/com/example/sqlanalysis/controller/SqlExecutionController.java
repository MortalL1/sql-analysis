package com.example.sqlanalysis.controller;

import com.example.sqlanalysis.common.Result;
import com.example.sqlanalysis.entity.SqlRequest;
import com.example.sqlanalysis.entity.SqlResult;
import com.example.sqlanalysis.service.SqlExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SQL执行控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/sql")
public class SqlExecutionController {

    @Autowired
    private SqlExecutionService sqlExecutionService;

    /**
     * 执行单条SQL
     * POST /api/sql/execute
     * Body: {"sql": "SELECT * FROM users", "timeout": 30}
     */
    @PostMapping("/execute")
    public Result<SqlResult> executeSql(@RequestBody SqlRequest request) {
        log.info("收到SQL执行请求: {}", request.getSql());
        
        try {
            SqlResult result = sqlExecutionService.executeSql(request);
            return Result.success("SQL执行成功", result);
        } catch (IllegalArgumentException e) {
            log.error("参数错误: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("SQL执行失败: {}", e.getMessage(), e);
            return Result.error("SQL执行失败: " + e.getMessage());
        }
    }

    /**
     * 批量执行SQL
     * POST /api/sql/batch
     * Body: [{"sql": "SELECT * FROM users"}, {"sql": "SELECT * FROM orders"}]
     */
    @PostMapping("/batch")
    public Result<List<SqlResult>> executeBatchSql(@RequestBody List<SqlRequest> requests) {
        log.info("收到批量SQL执行请求，数量: {}", requests.size());
        
        try {
            if (requests == null || requests.isEmpty()) {
                return Result.error(400, "SQL请求列表不能为空");
            }
            
            List<SqlResult> results = sqlExecutionService.executeBatchSql(requests);
            return Result.success("批量SQL执行完成", results);
        } catch (Exception e) {
            log.error("批量SQL执行失败: {}", e.getMessage(), e);
            return Result.error("批量SQL执行失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     * GET /api/sql/health
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("服务运行正常");
    }
}





