package com.example.sqlanalysis.executor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.sqlanalysis.entity.SqlRequest;
import com.example.sqlanalysis.entity.SqlResult;
import com.example.sqlanalysis.enums.DataSourceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis查询执行器
 */
@Slf4j
@Component
public class RedisExecutor implements QueryExecutor, InitializingBean {

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public SqlResult execute(SqlRequest request) {
        if (stringRedisTemplate == null) {
            throw new RuntimeException("Redis未配置，无法执行查询");
        }

        String command = request.getSql();
        if (command == null || command.trim().isEmpty()) {
            throw new IllegalArgumentException("Redis命令不能为空");
        }

        command = command.trim();
        log.info("开始执行Redis命令: {}", command);
        long startTime = System.currentTimeMillis();

        try {
            JSONObject jsonCommand;
            String operation;

            // 判断是JSON格式还是原生Redis命令
            if (command.startsWith("{")) {
                // JSON格式：{"command": "GET", "key": "user:1"}
                jsonCommand = JSON.parseObject(command);
                operation = jsonCommand.getString("command");
                if (operation == null || operation.trim().isEmpty()) {
                    throw new IllegalArgumentException("command字段不能为空");
                }
            } else {
                // 原生Redis命令格式：GET user:1 或 SET age 10
                jsonCommand = parseNativeCommand(command);
                operation = jsonCommand.getString("command");
            }

            SqlResult result = new SqlResult();
            result.setSql(command);
            result.setSqlType("REDIS_" + operation.toUpperCase());

            // 根据命令类型执行不同的操作
            switch (operation.toUpperCase()) {
                case "GET":
                    executeGet(jsonCommand, result);
                    break;
                case "SET":
                    executeSet(jsonCommand, result);
                    break;
                case "DEL":
                case "DELETE":
                    executeDelete(jsonCommand, result);
                    break;
                case "EXISTS":
                    executeExists(jsonCommand, result);
                    break;
                case "KEYS":
                    executeKeys(jsonCommand, result);
                    break;
                case "HGET":
                    executeHashGet(jsonCommand, result);
                    break;
                case "HSET":
                    executeHashSet(jsonCommand, result);
                    break;
                case "HGETALL":
                    executeHashGetAll(jsonCommand, result);
                    break;
                case "LRANGE":
                    executeListRange(jsonCommand, result);
                    break;
                case "LPUSH":
                case "RPUSH":
                    executeListPush(jsonCommand, result, operation.toUpperCase());
                    break;
                case "SMEMBERS":
                    executeSetMembers(jsonCommand, result);
                    break;
                case "SADD":
                    executeSetAdd(jsonCommand, result);
                    break;
                case "TTL":
                    executeTTL(jsonCommand, result);
                    break;
                case "EXPIRE":
                    executeExpire(jsonCommand, result);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的Redis命令: " + operation);
            }

            long endTime = System.currentTimeMillis();
            result.setExecutionTime(endTime - startTime);
            log.info("Redis命令执行成功，耗时: {}ms", result.getExecutionTime());

            return result;
        } catch (Exception e) {
            log.error("Redis命令执行失败: {}", e.getMessage(), e);
            throw new RuntimeException("Redis命令执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析原生Redis命令为JSON格式
     * 例如：GET user:1 -> {"command": "GET", "key": "user:1"}
     * SET age 10 -> {"command": "SET", "key": "age", "value": "10"}
     */
    private JSONObject parseNativeCommand(String command) {
        String[] parts = command.split("\\s+");
        if (parts.length == 0) {
            throw new IllegalArgumentException("Redis命令格式错误");
        }

        JSONObject json = new JSONObject();
        String cmd = parts[0].toUpperCase();
        json.put("command", cmd);

        try {
            switch (cmd) {
                case "GET":
                case "EXISTS":
                case "TTL":
                case "HGETALL":
                case "SMEMBERS":
                    // 命令格式：COMMAND key
                    if (parts.length < 2) {
                        throw new IllegalArgumentException(cmd + " 命令需要指定key");
                    }
                    json.put("key", parts[1]);
                    break;

                case "SET":
                    // 命令格式：SET key value [EX seconds]
                    if (parts.length < 3) {
                        throw new IllegalArgumentException("SET 命令格式：SET key value [EX seconds]");
                    }
                    json.put("key", parts[1]);
                    json.put("value", parts[2]);
                    // 检查是否有过期时间
                    if (parts.length >= 5 && "EX".equalsIgnoreCase(parts[3])) {
                        json.put("expire", Long.parseLong(parts[4]));
                    }
                    break;

                case "DEL":
                case "DELETE":
                    // 命令格式：DEL key
                    if (parts.length < 2) {
                        throw new IllegalArgumentException(cmd + " 命令需要指定key");
                    }
                    json.put("key", parts[1]);
                    break;

                case "KEYS":
                    // 命令格式：KEYS pattern
                    if (parts.length < 2) {
                        json.put("pattern", "*");
                    } else {
                        json.put("pattern", parts[1]);
                    }
                    break;

                case "HGET":
                    // 命令格式：HGET key field
                    if (parts.length < 3) {
                        throw new IllegalArgumentException("HGET 命令格式：HGET key field");
                    }
                    json.put("key", parts[1]);
                    json.put("field", parts[2]);
                    break;

                case "HSET":
                    // 命令格式：HSET key field value
                    if (parts.length < 4) {
                        throw new IllegalArgumentException("HSET 命令格式：HSET key field value");
                    }
                    json.put("key", parts[1]);
                    json.put("field", parts[2]);
                    json.put("value", parts[3]);
                    break;

                case "LRANGE":
                    // 命令格式：LRANGE key start end
                    if (parts.length < 4) {
                        throw new IllegalArgumentException("LRANGE 命令格式：LRANGE key start end");
                    }
                    json.put("key", parts[1]);
                    json.put("start", Long.parseLong(parts[2]));
                    json.put("end", Long.parseLong(parts[3]));
                    break;

                case "LPUSH":
                case "RPUSH":
                    // 命令格式：LPUSH key value [value ...]
                    if (parts.length < 3) {
                        throw new IllegalArgumentException(cmd + " 命令格式：" + cmd + " key value [value ...]");
                    }
                    json.put("key", parts[1]);
                    if (parts.length == 3) {
                        json.put("value", parts[2]);
                    } else {
                        JSONArray values = new JSONArray();
                        for (int i = 2; i < parts.length; i++) {
                            values.add(parts[i]);
                        }
                        json.put("values", values);
                    }
                    break;

                case "SADD":
                    // 命令格式：SADD key member [member ...]
                    if (parts.length < 3) {
                        throw new IllegalArgumentException("SADD 命令格式：SADD key member [member ...]");
                    }
                    json.put("key", parts[1]);
                    if (parts.length == 3) {
                        json.put("value", parts[2]);
                    } else {
                        JSONArray values = new JSONArray();
                        for (int i = 2; i < parts.length; i++) {
                            values.add(parts[i]);
                        }
                        json.put("values", values);
                    }
                    break;

                case "EXPIRE":
                    // 命令格式：EXPIRE key seconds
                    if (parts.length < 3) {
                        throw new IllegalArgumentException("EXPIRE 命令格式：EXPIRE key seconds");
                    }
                    json.put("key", parts[1]);
                    json.put("seconds", Long.parseLong(parts[2]));
                    break;

                default:
                    throw new IllegalArgumentException("不支持的Redis命令: " + cmd);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("命令参数格式错误：" + e.getMessage());
        }

        return json;
    }

    /**
     * 执行GET命令
     */
    private void executeGet(JSONObject jsonCommand, SqlResult result) {
        String key = jsonCommand.getString("key");
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("key参数不能为空");
        }

        String value = stringRedisTemplate.opsForValue().get(key);

        // 使用LinkedHashMap保持插入顺序
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("key", key);
        data.put("value", value);

        result.setData(Collections.singletonList(data));
        result.setColumns(Arrays.asList("key", "value"));
        result.setAffectedRows(1);
    }

    /**
     * 执行SET命令
     */
    private void executeSet(JSONObject jsonCommand, SqlResult result) {
        String key = jsonCommand.getString("key");
        String value = jsonCommand.getString("value");
        Long expire = jsonCommand.getLong("expire");

        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("key参数不能为空");
        }

        if (expire != null && expire > 0) {
            stringRedisTemplate.opsForValue().set(key, value, expire, TimeUnit.SECONDS);
        } else {
            stringRedisTemplate.opsForValue().set(key, value);
        }

        result.setData(new ArrayList<>());
        result.setColumns(new ArrayList<>());
        result.setAffectedRows(1);
    }

    /**
     * 执行DELETE命令
     */
    private void executeDelete(JSONObject jsonCommand, SqlResult result) {
        String key = jsonCommand.getString("key");
        JSONArray keys = jsonCommand.getJSONArray("keys");

        int count;
        if (keys != null && !keys.isEmpty()) {
            count = stringRedisTemplate.delete(keys.toJavaList(String.class)).intValue();
        } else if (key != null && !key.trim().isEmpty()) {
            count = stringRedisTemplate.delete(key) ? 1 : 0;
        } else {
            throw new IllegalArgumentException("key或keys参数不能为空");
        }

        result.setData(new ArrayList<>());
        result.setColumns(new ArrayList<>());
        result.setAffectedRows(count);
    }

    /**
     * 执行EXISTS命令
     */
    private void executeExists(JSONObject jsonCommand, SqlResult result) {
        String key = jsonCommand.getString("key");
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("key参数不能为空");
        }

        Boolean exists = stringRedisTemplate.hasKey(key);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("key", key);
        data.put("exists", exists != null && exists);

        result.setData(Collections.singletonList(data));
        result.setColumns(Arrays.asList("key", "exists"));
        result.setAffectedRows(1);
    }

    /**
     * 执行KEYS命令
     */
    private void executeKeys(JSONObject jsonCommand, SqlResult result) {
        String pattern = jsonCommand.getString("pattern");
        if (pattern == null || pattern.trim().isEmpty()) {
            pattern = "*";
        }

        Set<String> keys = stringRedisTemplate.keys(pattern);

        List<Map<String, Object>> data = new ArrayList<>();
        if (keys != null) {
            for (String key : keys) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("key", key);
                data.add(row);
            }
        }

        result.setData(data);
        result.setColumns(Collections.singletonList("key"));
        result.setAffectedRows(data.size());
    }

    /**
     * 执行HGET命令
     */
    private void executeHashGet(JSONObject jsonCommand, SqlResult result) {
        String key = jsonCommand.getString("key");
        String field = jsonCommand.getString("field");

        if (key == null || field == null) {
            throw new IllegalArgumentException("key和field参数不能为空");
        }

        Object value = stringRedisTemplate.opsForHash().get(key, field);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("key", key);
        data.put("field", field);
        data.put("value", value);

        result.setData(Collections.singletonList(data));
        result.setColumns(Arrays.asList("key", "field", "value"));
        result.setAffectedRows(1);
    }

    /**
     * 执行HSET命令
     */
    private void executeHashSet(JSONObject jsonCommand, SqlResult result) {
        String key = jsonCommand.getString("key");
        String field = jsonCommand.getString("field");
        String value = jsonCommand.getString("value");

        if (key == null || field == null) {
            throw new IllegalArgumentException("key和field参数不能为空");
        }

        stringRedisTemplate.opsForHash().put(key, field, value);

        result.setData(new ArrayList<>());
        result.setColumns(new ArrayList<>());
        result.setAffectedRows(1);
    }

    /**
     * 执行HGETALL命令
     */
    private void executeHashGetAll(JSONObject jsonCommand, SqlResult result) {
        String key = jsonCommand.getString("key");
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("key参数不能为空");
        }

        Map<Object, Object> hash = stringRedisTemplate.opsForHash().entries(key);

        List<Map<String, Object>> data = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : hash.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("field", entry.getKey());
            row.put("value", entry.getValue());
            data.add(row);
        }

        result.setData(data);
        result.setColumns(Arrays.asList("field", "value"));
        result.setAffectedRows(data.size());
    }

    /**
     * 执行LRANGE命令
     */
    private void executeListRange(JSONObject jsonCommand, SqlResult result) {
        String key = jsonCommand.getString("key");
        Long start = jsonCommand.getLong("start");
        Long end = jsonCommand.getLong("end");

        if (key == null) {
            throw new IllegalArgumentException("key参数不能为空");
        }

        start = start != null ? start : 0;
        end = end != null ? end : -1;

        List<String> list = stringRedisTemplate.opsForList().range(key, start, end);

        List<Map<String, Object>> data = new ArrayList<>();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("index", i);
                row.put("value", list.get(i));
                data.add(row);
            }
        }

        result.setData(data);
        result.setColumns(Arrays.asList("index", "value"));
        result.setAffectedRows(data.size());
    }

    /**
     * 执行LPUSH/RPUSH命令
     */
    private void executeListPush(JSONObject jsonCommand, SqlResult result, String operation) {
        String key = jsonCommand.getString("key");
        JSONArray values = jsonCommand.getJSONArray("values");
        String value = jsonCommand.getString("value");

        if (key == null) {
            throw new IllegalArgumentException("key参数不能为空");
        }

        long count;
        if ("LPUSH".equals(operation)) {
            if (values != null && !values.isEmpty()) {
                count = stringRedisTemplate.opsForList().leftPushAll(key, values.toArray(new String[0]));
            } else if (value != null) {
                count = stringRedisTemplate.opsForList().leftPush(key, value);
            } else {
                throw new IllegalArgumentException("value或values参数不能为空");
            }
        } else {
            if (values != null && !values.isEmpty()) {
                count = stringRedisTemplate.opsForList().rightPushAll(key, values.toArray(new String[0]));
            } else if (value != null) {
                count = stringRedisTemplate.opsForList().rightPush(key, value);
            } else {
                throw new IllegalArgumentException("value或values参数不能为空");
            }
        }

        result.setData(new ArrayList<>());
        result.setColumns(new ArrayList<>());
        result.setAffectedRows((int) count);
    }

    /**
     * 执行SMEMBERS命令
     */
    private void executeSetMembers(JSONObject jsonCommand, SqlResult result) {
        String key = jsonCommand.getString("key");
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("key参数不能为空");
        }

        Set<String> members = stringRedisTemplate.opsForSet().members(key);

        List<Map<String, Object>> data = new ArrayList<>();
        if (members != null) {
            for (String member : members) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("value", member);
                data.add(row);
            }
        }

        result.setData(data);
        result.setColumns(Collections.singletonList("value"));
        result.setAffectedRows(data.size());
    }

    /**
     * 执行SADD命令
     */
    private void executeSetAdd(JSONObject jsonCommand, SqlResult result) {
        String key = jsonCommand.getString("key");
        JSONArray values = jsonCommand.getJSONArray("values");
        String value = jsonCommand.getString("value");

        if (key == null) {
            throw new IllegalArgumentException("key参数不能为空");
        }

        long count;
        if (values != null && !values.isEmpty()) {
            count = stringRedisTemplate.opsForSet().add(key, values.toArray(new String[0]));
        } else if (value != null) {
            count = stringRedisTemplate.opsForSet().add(key, value);
        } else {
            throw new IllegalArgumentException("value或values参数不能为空");
        }

        result.setData(new ArrayList<>());
        result.setColumns(new ArrayList<>());
        result.setAffectedRows((int) count);
    }

    /**
     * 执行TTL命令
     */
    private void executeTTL(JSONObject jsonCommand, SqlResult result) {
        String key = jsonCommand.getString("key");
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("key参数不能为空");
        }

        Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("key", key);
        data.put("ttl", ttl);

        result.setData(Collections.singletonList(data));
        result.setColumns(Arrays.asList("key", "ttl"));
        result.setAffectedRows(1);
    }

    /**
     * 执行EXPIRE命令
     */
    private void executeExpire(JSONObject jsonCommand, SqlResult result) {
        String key = jsonCommand.getString("key");
        Long seconds = jsonCommand.getLong("seconds");

        if (key == null || seconds == null) {
            throw new IllegalArgumentException("key和seconds参数不能为空");
        }

        Boolean success = stringRedisTemplate.expire(key, seconds, TimeUnit.SECONDS);

        result.setData(new ArrayList<>());
        result.setColumns(new ArrayList<>());
        result.setAffectedRows(success != null && success ? 1 : 0);
    }

    @Override
    public boolean support(String dataSourceType) {
        return DataSourceType.REDIS.getCode().equalsIgnoreCase(dataSourceType);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String testStr = stringRedisTemplate.opsForValue().get("test");
        log.info("redis 连接初始化后预热,test:{}", testStr);
    }
}

