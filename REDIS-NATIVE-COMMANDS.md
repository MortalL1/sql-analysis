# Redis原生命令支持

## 概述

系统现在**完全支持原生Redis命令**，无需再使用复杂的JSON格式！

你可以直接输入标准的Redis命令，就像在 `redis-cli` 中一样。

## 支持的命令列表

### 1. 字符串操作

#### GET - 获取值
```
GET user:1
GET session:abc123
```

#### SET - 设置值
```
SET user:1 张三
SET age 25
SET session:123 token_value EX 3600
```
- 支持 `EX seconds` 参数设置过期时间

#### DEL/DELETE - 删除键
```
DEL user:1
DELETE cache:temp
```

#### EXISTS - 检查键是否存在
```
EXISTS user:1
EXISTS session:abc
```

### 2. 键管理

#### KEYS - 查找键
```
KEYS user:*
KEYS *
KEYS session:*
```

#### TTL - 查看剩余生存时间
```
TTL user:1
TTL session:123
```

#### EXPIRE - 设置过期时间
```
EXPIRE user:1 3600
EXPIRE cache:temp 300
```

### 3. Hash操作

#### HGET - 获取Hash字段值
```
HGET user:1 name
HGET user:1 age
```

#### HSET - 设置Hash字段值
```
HSET user:1 name 张三
HSET user:1 age 25
```

#### HGETALL - 获取Hash所有字段
```
HGETALL user:1
HGETALL settings:app
```

### 4. List操作

#### LRANGE - 获取列表元素
```
LRANGE list:1 0 -1
LRANGE queue:jobs 0 10
```
- start: 起始索引（0表示第一个）
- end: 结束索引（-1表示最后一个）

#### LPUSH - 从左侧推入
```
LPUSH list:1 item1
LPUSH queue:jobs job1 job2 job3
```

#### RPUSH - 从右侧推入
```
RPUSH list:1 item1
RPUSH queue:tasks task1 task2
```

### 5. Set操作

#### SMEMBERS - 获取集合所有成员
```
SMEMBERS set:tags
SMEMBERS users:online
```

#### SADD - 添加集合成员
```
SADD set:tags tag1
SADD users:online user1 user2 user3
```

## 在Web界面中使用

1. 打开 `sql-query-ui.html`
2. 选择数据源：`Redis`
3. 直接输入Redis命令：
   ```
   GET user:1
   ```
4. 点击"执行查询"

## 使用curl测试

```bash
# GET命令
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "GET user:1"
  }'

# SET命令
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "SET user:1 张三"
  }'

# KEYS命令
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "KEYS user:*"
  }'

# HSET命令
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "HSET user:1 name 张三"
  }'
```

## 命令格式规则

### 基本规则
- 命令不区分大小写（`GET`、`get`、`Get` 都可以）
- 参数之间用空格分隔
- 支持多个参数的命令

### 示例对比

#### ✅ 正确格式
```
GET user:1
SET age 25
HSET user:1 name 张三
LPUSH list:1 item1 item2
```

#### ❌ 错误格式
```
GETuser:1          # 缺少空格
SET age            # 缺少参数
HSET user:1 name   # HSET需要3个参数
```

## 仍然支持JSON格式

如果你更喜欢JSON格式，依然可以使用：

```json
{"command": "GET", "key": "user:1"}
{"command": "SET", "key": "age", "value": "25"}
{"command": "HSET", "key": "user:1", "field": "name", "value": "张三"}
```

系统会自动识别输入格式并正确处理。

## 优势对比

### 原生命令方式（推荐）⭐
```
GET user:1
```
- ✅ 简单直观
- ✅ 符合Redis习惯
- ✅ 输入快速
- ✅ 易于记忆

### JSON方式
```json
{"command": "GET", "key": "user:1"}
```
- ✅ 结构化
- ✅ 适合程序调用
- ✅ 参数明确

## 常见用法示例

### 缓存管理
```bash
# 设置缓存（10分钟过期）
SET cache:user:1 "{\"name\":\"张三\",\"age\":25}" EX 600

# 获取缓存
GET cache:user:1

# 检查缓存是否存在
EXISTS cache:user:1

# 查看缓存剩余时间
TTL cache:user:1

# 删除缓存
DEL cache:user:1
```

### 会话管理
```bash
# 创建会话（1小时过期）
SET session:abc123 user_token_here EX 3600

# 获取会话
GET session:abc123

# 续期会话
EXPIRE session:abc123 3600

# 删除会话
DEL session:abc123
```

### 用户信息存储
```bash
# 存储用户信息（Hash结构）
HSET user:1001 name 张三
HSET user:1001 age 25
HSET user:1001 email zhangsan@example.com

# 获取单个字段
HGET user:1001 name

# 获取所有字段
HGETALL user:1001
```

### 队列操作
```bash
# 向队列添加任务
RPUSH queue:tasks task1
RPUSH queue:tasks task2 task3

# 查看队列内容
LRANGE queue:tasks 0 -1

# 添加到队列头部（优先任务）
LPUSH queue:tasks urgent_task
```

### 集合操作
```bash
# 添加标签
SADD tags:article:1 技术
SADD tags:article:1 编程 Redis

# 查看所有标签
SMEMBERS tags:article:1

# 添加在线用户
SADD users:online user1 user2 user3

# 查看在线用户
SMEMBERS users:online
```

## 注意事项

1. **参数中包含空格**：如果值包含空格，请使用JSON格式
   ```json
   {"command": "SET", "key": "user:1", "value": "张 三"}
   ```

2. **特殊字符**：包含特殊字符的值建议使用JSON格式

3. **大量参数**：如LPUSH多个值时，可以用空格分隔：
   ```
   LPUSH list:1 val1 val2 val3 val4 val5
   ```

4. **命令不存在**：如果输入不支持的命令，会提示错误

## 错误处理

### 常见错误

#### 1. 参数不足
```
❌ SET user:1
✅ SET user:1 value
```

#### 2. 参数格式错误
```
❌ EXPIRE user:1 abc
✅ EXPIRE user:1 3600
```

#### 3. 命令不支持
```
❌ ZADD sorted:set 1 member
```
提示：目前不支持的命令可以使用JSON格式或提交功能请求

## 总结

原生Redis命令支持让使用更加简单直观！

- 🎯 直接输入Redis命令
- 🚀 快速执行查询
- 💡 符合使用习惯
- ✨ 降低学习成本

享受使用吧！🎉



