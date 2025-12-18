package com.example.sqlanalysis.executor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.sqlanalysis.entity.SqlRequest;
import com.example.sqlanalysis.entity.SqlResult;
import com.example.sqlanalysis.enums.DataSourceType;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * MongoDB查询执行器
 */
@Slf4j
@Component
public class MongoDBExecutor implements QueryExecutor {

    @Autowired(required = false)
    private MongoTemplate mongoTemplate;

    @Override
    public SqlResult execute(SqlRequest request) {
        if (mongoTemplate == null) {
            throw new RuntimeException("MongoDB未配置，无法执行查询");
        }

        String command = request.getSql();
        if (command == null || command.trim().isEmpty()) {
            throw new IllegalArgumentException("MongoDB查询命令不能为空");
        }

        log.info("开始执行MongoDB查询: {}", command);
        long startTime = System.currentTimeMillis();

        try {
            // 解析JSON命令
            JSONObject jsonCommand = JSON.parseObject(command);
            String collection = jsonCommand.getString("collection");
            String operation = jsonCommand.getString("operation");

            if (collection == null || collection.trim().isEmpty()) {
                throw new IllegalArgumentException("collection字段不能为空");
            }

            SqlResult result = new SqlResult();
            result.setSql(command);
            result.setSqlType("MONGODB_" + (operation != null ? operation.toUpperCase() : "QUERY"));

            // 根据操作类型执行不同的查询
            switch (operation != null ? operation.toLowerCase() : "find") {
                case "find":
                    executeFindQuery(jsonCommand, collection, result);
                    break;
                case "count":
                    executeCountQuery(jsonCommand, collection, result);
                    break;
                case "aggregate":
                    executeAggregateQuery(jsonCommand, collection, result);
                    break;
                case "insert":
                    executeInsert(jsonCommand, collection, result);
                    break;
                case "update":
                    executeUpdate(jsonCommand, collection, result);
                    break;
                case "delete":
                    executeDelete(jsonCommand, collection, result);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的MongoDB操作: " + operation);
            }

            long endTime = System.currentTimeMillis();
            result.setExecutionTime(endTime - startTime);
            log.info("MongoDB查询执行成功，耗时: {}ms", result.getExecutionTime());

            return result;
        } catch (Exception e) {
            log.error("MongoDB查询执行失败: {}", e.getMessage(), e);
            throw new RuntimeException("MongoDB查询执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行find查询
     */
    private void executeFindQuery(JSONObject jsonCommand, String collection, SqlResult result) {
        String queryStr = jsonCommand.getString("query");
        String fieldsStr = jsonCommand.getString("fields");
        Integer limit = jsonCommand.getInteger("limit");
        Integer skip = jsonCommand.getInteger("skip");

        // 构建查询
        Query query;
        if (queryStr != null && !queryStr.trim().isEmpty()) {
            query = new BasicQuery(queryStr, fieldsStr);
        } else {
            query = new Query();
        }

        if (limit != null && limit > 0) {
            query.limit(limit);
        }
        if (skip != null && skip > 0) {
            query.skip(skip);
        }

        // 执行查询
        List<Document> documents = mongoTemplate.find(query, Document.class, collection);
        
        // 转换结果（使用LinkedHashMap保持字段顺序）
        List<Map<String, Object>> data = new ArrayList<>();
        Set<String> allColumns = new LinkedHashSet<>();
        
        for (Document doc : documents) {
            // Document本身就保持插入顺序，转换为LinkedHashMap保持顺序
            Map<String, Object> row = new LinkedHashMap<>(doc);
            allColumns.addAll(row.keySet());
            data.add(row);
        }

        result.setData(data);
        result.setColumns(new ArrayList<>(allColumns));
        result.setAffectedRows(data.size());
    }

    /**
     * 执行count查询
     */
    private void executeCountQuery(JSONObject jsonCommand, String collection, SqlResult result) {
        String queryStr = jsonCommand.getString("query");
        
        Query query;
        if (queryStr != null && !queryStr.trim().isEmpty()) {
            query = new BasicQuery(queryStr);
        } else {
            query = new Query();
        }

        long count = mongoTemplate.count(query, collection);
        
        Map<String, Object> countResult = new LinkedHashMap<>();
        countResult.put("count", count);
        
        result.setData(Collections.singletonList(countResult));
        result.setColumns(Collections.singletonList("count"));
        result.setAffectedRows(1);
    }

    /**
     * 执行aggregate查询
     */
    private void executeAggregateQuery(JSONObject jsonCommand, String collection, SqlResult result) {
        String pipelineStr = jsonCommand.getString("pipeline");
        if (pipelineStr == null || pipelineStr.trim().isEmpty()) {
            throw new IllegalArgumentException("aggregate操作需要pipeline参数");
        }

        // 这里简化处理，实际应该解析pipeline
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> placeholder = new LinkedHashMap<>();
        placeholder.put("message", "aggregate功能需要根据具体pipeline实现");
        data.add(placeholder);

        result.setData(data);
        result.setColumns(Collections.singletonList("message"));
        result.setAffectedRows(1);
    }

    /**
     * 执行insert操作
     */
    private void executeInsert(JSONObject jsonCommand, String collection, SqlResult result) {
        String documentStr = jsonCommand.getString("document");
        if (documentStr == null || documentStr.trim().isEmpty()) {
            throw new IllegalArgumentException("insert操作需要document参数");
        }

        Document document = Document.parse(documentStr);
        mongoTemplate.insert(document, collection);

        result.setData(new ArrayList<>());
        result.setColumns(new ArrayList<>());
        result.setAffectedRows(1);
    }

    /**
     * 执行update操作
     */
    private void executeUpdate(JSONObject jsonCommand, String collection, SqlResult result) {
        String queryStr = jsonCommand.getString("query");
        String updateStr = jsonCommand.getString("update");

        if (queryStr == null || updateStr == null) {
            throw new IllegalArgumentException("update操作需要query和update参数");
        }

        Query query = new BasicQuery(queryStr);
        Document update = Document.parse(updateStr);
        
        // 简化处理，实际应该使用UpdateResult
        long count = mongoTemplate.updateMulti(query, 
            org.springframework.data.mongodb.core.query.Update.fromDocument(update), 
            collection).getModifiedCount();

        result.setData(new ArrayList<>());
        result.setColumns(new ArrayList<>());
        result.setAffectedRows((int) count);
    }

    /**
     * 执行delete操作
     */
    private void executeDelete(JSONObject jsonCommand, String collection, SqlResult result) {
        String queryStr = jsonCommand.getString("query");
        if (queryStr == null || queryStr.trim().isEmpty()) {
            throw new IllegalArgumentException("delete操作需要query参数");
        }

        Query query = new BasicQuery(queryStr);
        long count = mongoTemplate.remove(query, collection).getDeletedCount();

        result.setData(new ArrayList<>());
        result.setColumns(new ArrayList<>());
        result.setAffectedRows((int) count);
    }

    @Override
    public boolean support(String dataSourceType) {
        return DataSourceType.MONGODB.getCode().equalsIgnoreCase(dataSourceType);
    }
}

