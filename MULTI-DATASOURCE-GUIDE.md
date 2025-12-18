# 多数据源查询指南

本系统支持多种数据源查询，包括MySQL、MongoDB和Redis。

## 支持的数据源类型

- `mysql` - MySQL关系型数据库（默认）
- `mongodb` - MongoDB文档数据库
- `redis` - Redis缓存数据库

## 一、MySQL查询

### 配置
在 `application.properties` 中配置：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/test
spring.datasource.username=root
spring.datasource.password=root
```

### 使用示例

#### 1. SELECT查询
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "mysql",
    "sql": "SELECT * FROM user WHERE age > 25"
  }'
```

#### 2. INSERT操作
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "mysql",
    "sql": "INSERT INTO user (name, age, email) VALUES (\"张三\", 25, \"zhangsan@example.com\")"
  }'
```

#### 3. UPDATE操作
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "mysql",
    "sql": "UPDATE users SET age = 26 WHERE name = \"张三\""
  }'
```

## 二、MongoDB查询

### 配置
在 `application.properties` 中配置：
```properties
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=test
```

### 命令格式
MongoDB查询使用JSON格式，基本结构：
```json
{
  "collection": "集合名称",
  "operation": "操作类型",
  ...其他参数
}
```

### 使用示例

#### 1. find查询 - 查询所有文档
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "mongodb",
    "sql": "{\"collection\": \"users\", \"operation\": \"find\"}"
  }'
```

#### 2. find查询 - 带条件
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "mongodb",
    "sql": "{\"collection\": \"users\", \"operation\": \"find\", \"query\": \"{\\\"age\\\": {\\\"$gt\\\": 25}}\"}"
  }'
```

#### 3. find查询 - 带限制和跳过
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "mongodb",
    "sql": "{\"collection\": \"users\", \"operation\": \"find\", \"limit\": 10, \"skip\": 0}"
  }'
```

#### 4. count查询 - 统计文档数
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "mongodb",
    "sql": "{\"collection\": \"users\", \"operation\": \"count\", \"query\": \"{\\\"age\\\": {\\\"$gt\\\": 25}}\"}"
  }'
```

#### 5. insert操作 - 插入文档
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "mongodb",
    "sql": "{\"collection\": \"users\", \"operation\": \"insert\", \"document\": \"{\\\"name\\\": \\\"李四\\\", \\\"age\\\": 30, \\\"email\\\": \\\"lisi@example.com\\\"}\"}"
  }'
```

#### 6. update操作 - 更新文档
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "mongodb",
    "sql": "{\"collection\": \"users\", \"operation\": \"update\", \"query\": \"{\\\"name\\\": \\\"李四\\\"}\", \"update\": \"{\\\"$set\\\": {\\\"age\\\": 31}}\"}"
  }'
```

#### 7. delete操作 - 删除文档
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "mongodb",
    "sql": "{\"collection\": \"users\", \"operation\": \"delete\", \"query\": \"{\\\"age\\\": {\\\"$gt\\\": 100}}\"}"
  }'
```

## 三、Redis查询

### 配置
在 `application.properties` 中配置：
```properties
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.database=0
```

### 命令格式

⭐ **支持两种输入方式**：

#### 方式1：原生Redis命令（推荐）
直接输入标准的Redis命令，更简单直观：
```
GET user:1
SET user:1 张三
KEYS user:*
```

#### 方式2：JSON格式
使用JSON格式传递参数：
```json
{
  "command": "命令类型",
  ...其他参数
}
```

### 使用示例

#### 1. GET - 获取字符串值

**原生命令方式**（推荐）：
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "GET user:1"
  }'
```

**JSON方式**：
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "{\"command\": \"GET\", \"key\": \"user:1\"}"
  }'
```

#### 2. SET - 设置字符串值

**原生命令方式**（推荐）：
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "SET user:1 张三"
  }'
```

**JSON方式**：
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "{\"command\": \"SET\", \"key\": \"user:1\", \"value\": \"张三\"}"
  }'
```

#### 3. SET - 设置带过期时间的值

**原生命令方式**（推荐）：
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "SET session:123 token_value EX 3600"
  }'
```

**JSON方式**：
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "{\"command\": \"SET\", \"key\": \"session:123\", \"value\": \"token_value\", \"expire\": 3600}"
  }'
```

#### 4. DEL - 删除键

**原生命令方式**（推荐）：
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "DEL user:1"
  }'
```

**JSON方式**：
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "{\"command\": \"DEL\", \"key\": \"user:1\"}"
  }'
```

#### 5. EXISTS - 检查键是否存在
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "{\"command\": \"EXISTS\", \"key\": \"user:1\"}"
  }'
```

#### 6. KEYS - 查找键
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "{\"command\": \"KEYS\", \"pattern\": \"user:*\"}"
  }'
```

#### 7. TTL - 查看过期时间
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "{\"command\": \"TTL\", \"key\": \"session:123\"}"
  }'
```

#### 8. EXPIRE - 设置过期时间
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "{\"command\": \"EXPIRE\", \"key\": \"user:1\", \"seconds\": 3600}"
  }'
```

#### 9. HGET - 获取Hash字段值
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "{\"command\": \"HGET\", \"key\": \"user:1\", \"field\": \"name\"}"
  }'
```

#### 10. HSET - 设置Hash字段值
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "{\"command\": \"HSET\", \"key\": \"user:1\", \"field\": \"name\", \"value\": \"张三\"}"
  }'
```

#### 11. HGETALL - 获取Hash所有字段
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "{\"command\": \"HGETALL\", \"key\": \"user:1\"}"
  }'
```

#### 12. LRANGE - 获取列表元素
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "{\"command\": \"LRANGE\", \"key\": \"list:1\", \"start\": 0, \"end\": -1}"
  }'
```

#### 13. LPUSH - 从左侧推入列表
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "{\"command\": \"LPUSH\", \"key\": \"list:1\", \"value\": \"item1\"}"
  }'
```

#### 14. RPUSH - 从右侧推入列表
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "{\"command\": \"RPUSH\", \"key\": \"list:1\", \"values\": [\"item1\", \"item2\", \"item3\"]}"
  }'
```

#### 15. SMEMBERS - 获取集合所有成员
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "{\"command\": \"SMEMBERS\", \"key\": \"set:1\"}"
  }'
```

#### 16. SADD - 添加集合成员
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "{\"command\": \"SADD\", \"key\": \"set:1\", \"values\": [\"member1\", \"member2\"]}"
  }'
```

## 四、批量查询

支持在一次请求中执行多个不同数据源的查询：

```bash
curl -X POST http://localhost:8080/api/sql/batch \
  -H "Content-Type: application/json" \
  -d '[
    {
      "dataSourceType": "mysql",
      "sql": "SELECT COUNT(*) as total FROM users"
    },
    {
      "dataSourceType": "mongodb",
      "sql": "{\"collection\": \"orders\", \"operation\": \"count\"}"
    },
    {
      "dataSourceType": "redis",
      "sql": "{\"command\": \"GET\", \"key\": \"cache:stats\"}"
    }
  ]'
```

## 五、响应格式

所有查询都返回统一的格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "success": true,
  "data": {
    "sqlType": "数据源_操作类型",
    "affectedRows": 影响的行数,
    "data": [
      {
        "字段1": "值1",
        "字段2": "值2"
      }
    ],
    "columns": ["字段1", "字段2"],
    "executionTime": 125,
    "sql": "原始查询语句"
  }
}
```

## 六、注意事项

1. **MySQL**: 
   - 支持标准SQL语句
   - 注意SQL注入风险，建议使用参数化查询
   
2. **MongoDB**:
   - 查询条件需要使用MongoDB查询语法
   - JSON中的引号需要转义
   - 支持$gt, $lt, $eq等操作符

3. **Redis**:
   - 命令名称不区分大小写
   - 支持基本的数据类型操作：String、Hash、List、Set
   - 过期时间单位为秒

4. **安全建议**:
   - 生产环境建议添加认证和授权
   - 限制可执行的命令类型
   - 添加查询日志审计
   - 设置查询超时限制

## 七、测试数据准备

### MySQL测试数据
```sql
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50),
    age INT,
    email VARCHAR(100)
);

INSERT INTO users (name, age, email) VALUES
('张三', 25, 'zhangsan@example.com'),
('李四', 30, 'lisi@example.com'),
('王五', 28, 'wangwu@example.com');
```

### MongoDB测试数据
```javascript
db.users.insertMany([
  {name: "张三", age: 25, email: "zhangsan@example.com"},
  {name: "李四", age: 30, email: "lisi@example.com"},
  {name: "王五", age: 28, email: "wangwu@example.com"}
]);
```

### Redis测试数据
```bash
redis-cli
SET user:1 "张三"
SET user:2 "李四"
HSET user:1:info name "张三" age 25 email "zhangsan@example.com"
```

