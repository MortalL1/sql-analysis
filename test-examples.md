# SQL执行系统测试示例

## 测试前准备

1. 确保MySQL数据库已启动
2. 创建测试数据库和表：

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS test DEFAULT CHARACTER SET utf8mb4;

-- 使用数据库
USE test;

-- 创建测试表
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    age INT,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 插入测试数据
INSERT INTO users (name, age, email) VALUES
('张三', 25, 'zhangsan@example.com'),
('李四', 30, 'lisi@example.com'),
('王五', 28, 'wangwu@example.com');
```

## 测试用例

### 1. 健康检查

```bash
curl http://localhost:8080/api/sql/health
```

### 2. 执行SELECT查询

```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "SELECT * FROM users"
  }'
```

### 3. 执行带条件的查询

```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "SELECT * FROM users WHERE age > 25"
  }'
```

### 4. 执行COUNT查询

```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "SELECT COUNT(*) as total FROM users"
  }'
```

### 5. 执行INSERT操作

```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "INSERT INTO users (name, age, email) VALUES (\"赵六\", 35, \"zhaoliu@example.com\")"
  }'
```

### 6. 执行UPDATE操作

```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "UPDATE users SET age = 26 WHERE name = \"张三\""
  }'
```

### 7. 执行DELETE操作

```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "DELETE FROM users WHERE age > 100"
  }'
```

### 8. 批量执行SQL

```bash
curl -X POST http://localhost:8080/api/sql/batch \
  -H "Content-Type: application/json" \
  -d '[
    {
      "sql": "SELECT COUNT(*) as total FROM users"
    },
    {
      "sql": "SELECT * FROM users WHERE age > 25"
    },
    {
      "sql": "SELECT AVG(age) as avg_age FROM users"
    }
  ]'
```

### 9. 设置超时时间

```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "SELECT * FROM users",
    "timeout": 10
  }'
```

### 10. 执行JOIN查询（如果有相关表）

```bash
# 先创建订单表
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "CREATE TABLE IF NOT EXISTS orders (id INT AUTO_INCREMENT PRIMARY KEY, user_id INT, amount DECIMAL(10,2), order_date DATE)"
  }'

# 执行JOIN查询
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "SELECT u.name, COUNT(o.id) as order_count FROM users u LEFT JOIN orders o ON u.id = o.user_id GROUP BY u.id, u.name"
  }'
```

## 使用Postman测试

### 配置请求

1. **Method**: POST
2. **URL**: `http://localhost:8080/api/sql/execute`
3. **Headers**: 
   - Content-Type: application/json
4. **Body** (raw JSON):

```json
{
  "sql": "SELECT * FROM users",
  "timeout": 30
}
```

## 使用Python测试

```python
import requests
import json

# 基础URL
base_url = "http://localhost:8080/api/sql"

# 执行单条SQL
def execute_sql(sql, timeout=30):
    url = f"{base_url}/execute"
    data = {
        "sql": sql,
        "timeout": timeout
    }
    response = requests.post(url, json=data)
    return response.json()

# 批量执行SQL
def execute_batch(sql_list):
    url = f"{base_url}/batch"
    data = [{"sql": sql} for sql in sql_list]
    response = requests.post(url, json=data)
    return response.json()

# 测试
if __name__ == "__main__":
    # 测试查询
    result = execute_sql("SELECT * FROM users")
    print(json.dumps(result, indent=2, ensure_ascii=False))
    
    # 测试批量执行
    sqls = [
        "SELECT COUNT(*) FROM users",
        "SELECT * FROM users LIMIT 5"
    ]
    batch_result = execute_batch(sqls)
    print(json.dumps(batch_result, indent=2, ensure_ascii=False))
```

## 响应格式说明

### 成功响应示例

```json
{
  "code": 200,
  "message": "SQL执行成功",
  "success": true,
  "data": {
    "sqlType": "SELECT",
    "affectedRows": 3,
    "data": [
      {
        "id": 1,
        "name": "张三",
        "age": 25,
        "email": "zhangsan@example.com",
        "created_at": "2024-01-01T10:00:00"
      }
    ],
    "columns": ["id", "name", "age", "email", "created_at"],
    "executionTime": 125,
    "sql": "SELECT * FROM users"
  }
}
```

### 错误响应示例

```json
{
  "code": 500,
  "message": "SQL执行失败: Table 'test.xxx' doesn't exist",
  "success": false,
  "data": null
}
```

## 性能测试

```bash
# 使用ab进行压力测试
ab -n 1000 -c 10 -p request.json -T application/json http://localhost:8080/api/sql/execute
```

request.json 内容：
```json
{"sql":"SELECT * FROM users LIMIT 10","timeout":30}
```

## 注意事项

1. 生产环境请勿使用此系统执行危险SQL（DROP、TRUNCATE等）
2. 建议添加SQL白名单验证
3. 建议添加用户认证和权限控制
4. 对于大数据量查询，建议添加LIMIT限制
5. 注意SQL注入风险，生产环境必须添加安全防护





