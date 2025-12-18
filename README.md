# SQL分析执行系统（多数据源版本）

## 项目简介

这是一个基于Spring Boot的多数据源查询执行系统，支持MySQL、MongoDB、Redis等多种数据源的查询和操作。

## 技术栈

- Spring Boot 2.6.13
- Spring JDBC
- Spring Data MongoDB
- Spring Data Redis
- MySQL Driver
- Jedis
- Lombok
- FastJSON

## 功能特性

- ✅ 支持多种数据源（MySQL、MongoDB、Redis）
- ✅ 执行SQL语句（SELECT、INSERT、UPDATE、DELETE等）
- ✅ MongoDB文档查询和操作
- ✅ Redis缓存查询和操作（⭐ 支持原生命令！）
- ✅ 批量执行跨数据源查询
- ✅ 查询结果自动解析
- ✅ 执行时间统计
- ✅ 超时控制
- ✅ 统一响应格式
- ✅ 全局异常处理
- ✅ 美观的Web查询界面

## 快速开始

**⚡ 新手？** 查看 [QUICK-START.md](QUICK-START.md) 获取5分钟快速启动指南！

### 1. 配置数据源

编辑 `src/main/resources/application.properties` 文件，配置需要使用的数据源：

#### MySQL配置
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/test
spring.datasource.username=root
spring.datasource.password=root
```

#### MongoDB配置（可选）
```properties
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=test
```

#### Redis配置（可选）
```properties
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.database=0
```

### 2. 启动项目

```bash
mvn spring-boot:run
```

或者使用IDE直接运行 `SqlAnalysisApplication` 类。

### 3. 使用Web界面（推荐）

打开浏览器访问项目根目录下的 `sql-query-ui.html` 文件：

```bash
open sql-query-ui.html  # macOS
# 或直接在浏览器中打开该文件
```

Web界面功能：
- ✅ 可视化查询界面
- ✅ 数据源类型选择（MySQL/MongoDB/Redis）
- ✅ 查询示例快速填充
- ✅ 表格数据展示
- ✅ 自动分页（每页100条）
- ✅ 错误信息展示
- ✅ 执行统计信息

### 4. 使用命令行测试

#### 执行MySQL查询

```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "mysql",
    "sql": "SELECT * FROM users LIMIT 10"
  }'
```

#### 执行MongoDB查询

```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "mongodb",
    "sql": "{\"collection\": \"users\", \"operation\": \"find\", \"limit\": 10}"
  }'
```

#### 执行Redis命令

**原生命令方式**（推荐）：
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "GET user:1"
  }'
```

**JSON格式方式**：
```bash
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "redis",
    "sql": "{\"command\": \"GET\", \"key\": \"user:1\"}"
  }'
```

#### 批量执行跨数据源查询

```bash
curl -X POST http://localhost:8080/api/sql/batch \
  -H "Content-Type: application/json" \
  -d '[
    {
      "dataSourceType": "mysql",
      "sql": "SELECT COUNT(*) FROM users"
    },
    {
      "dataSourceType": "mongodb",
      "sql": "{\"collection\": \"orders\", \"operation\": \"count\"}"
    },
    {
      "dataSourceType": "redis",
      "sql": "{\"command\": \"GET\", \"key\": \"stats\"}"
    }
  ]'
```

#### 健康检查

```bash
curl http://localhost:8080/api/sql/health
```

## Web界面使用说明

项目提供了一个美观的Web界面 `sql-query-ui.html`，使用步骤：

1. **启动后端服务**
   ```bash
   mvn spring-boot:run
   ```

2. **打开Web界面**
   - 直接双击 `sql-query-ui.html` 文件
   - 或在浏览器地址栏输入文件路径

3. **执行查询**
   - 选择数据源类型（MySQL/MongoDB/Redis）
   - 输入查询语句（可以点击示例快速填充）
   - 点击"执行查询"按钮
   - 快捷键：Ctrl + Enter 执行查询

4. **查看结果**
   - 成功：显示表格数据，支持分页浏览
   - 失败：显示错误信息

**详细使用说明**：请查看 [WEB-UI-GUIDE.md](WEB-UI-GUIDE.md)

## API文档

### 1. 执行查询

**接口地址：** `POST /api/sql/execute`

**请求参数：**

```json
{
  "dataSourceType": "mysql",
  "sql": "SELECT * FROM users",
  "timeout": 30
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dataSourceType | String | 否 | 数据源类型：mysql/mongodb/redis，默认mysql |
| sql | String | 是 | 查询语句/命令 |
| timeout | Integer | 否 | 超时时间（秒），默认30秒 |

**响应示例：**

```json
{
  "code": 200,
  "message": "SQL执行成功",
  "success": true,
  "data": {
    "sqlType": "SELECT",
    "affectedRows": 2,
    "data": [
      {
        "id": 1,
        "name": "张三",
        "age": 25
      },
      {
        "id": 2,
        "name": "李四",
        "age": 30
      }
    ],
    "columns": ["id", "name", "age"],
    "executionTime": 125,
    "sql": "SELECT * FROM users"
  }
}
```

### 2. 批量执行查询

**接口地址：** `POST /api/sql/batch`

**请求参数：**

```json
[
  {
    "dataSourceType": "mysql",
    "sql": "SELECT COUNT(*) FROM users"
  },
  {
    "dataSourceType": "mongodb",
    "sql": "{\"collection\": \"orders\", \"operation\": \"find\"}"
  },
  {
    "dataSourceType": "redis",
    "sql": "{\"command\": \"GET\", \"key\": \"cache:stats\"}"
  }
]
```

**响应示例：**

```json
{
  "code": 200,
  "message": "批量SQL执行完成",
  "success": true,
  "data": [
    {
      "sqlType": "SELECT",
      "affectedRows": 1,
      "data": [{"COUNT(*)": 100}],
      "columns": ["COUNT(*)"],
      "executionTime": 50,
      "sql": "SELECT COUNT(*) FROM users"
    },
    {
      "sqlType": "INSERT",
      "affectedRows": 1,
      "data": [],
      "columns": [],
      "executionTime": 30,
      "sql": "INSERT INTO users (name, age) VALUES ('王五', 28)"
    }
  ]
}
```

### 3. 健康检查

**接口地址：** `GET /api/sql/health`

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "success": true,
  "data": "服务运行正常"
}
```

## 项目结构

```
sql-analysis/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── sqlanalysis/
│   │   │               ├── SqlAnalysisApplication.java      # 主启动类
│   │   │               ├── common/
│   │   │               │   └── Result.java                  # 统一响应结果类
│   │   │               ├── controller/
│   │   │               │   └── SqlExecutionController.java  # 查询执行控制器
│   │   │               ├── entity/
│   │   │               │   ├── SqlRequest.java              # 查询请求实体
│   │   │               │   └── SqlResult.java               # 查询结果实体
│   │   │               ├── enums/
│   │   │               │   └── DataSourceType.java          # 数据源类型枚举
│   │   │               ├── exception/
│   │   │               │   └── GlobalExceptionHandler.java  # 全局异常处理
│   │   │               ├── executor/
│   │   │               │   ├── QueryExecutor.java           # 查询执行器接口
│   │   │               │   ├── MySQLExecutor.java           # MySQL执行器
│   │   │               │   ├── MongoDBExecutor.java         # MongoDB执行器
│   │   │               │   └── RedisExecutor.java           # Redis执行器
│   │   │               └── service/
│   │   │                   └── SqlExecutionService.java     # 查询执行服务
│   │   └── resources/
│   │       └── application.properties                        # 配置文件
│   └── test/
│       └── java/
├── pom.xml                                                    # Maven配置
├── README.md                                                  # 项目说明
├── QUICK-START.md                                             # 快速开始指南 ⭐
├── MULTI-DATASOURCE-GUIDE.md                                 # 多数据源使用指南
├── REDIS-NATIVE-COMMANDS.md                                   # Redis原生命令支持 ⭐ NEW
├── WEB-UI-GUIDE.md                                            # Web界面使用指南
├── test-examples.md                                           # 测试示例
└── sql-query-ui.html                                          # Web查询界面 ⭐
```

## 注意事项

⚠️ **安全警告：**

1. 本项目允许执行任意SQL语句，存在严重的安全风险
2. 建议仅在开发、测试环境使用
3. 生产环境使用时，必须添加以下安全措施：
   - SQL语句白名单验证
   - 权限认证和授权
   - SQL注入防护
   - 敏感操作审计
   - 限制可执行的SQL类型（如禁止DROP、TRUNCATE等）

## 多数据源使用指南

详细的多数据源使用方法和示例，请查看以下文档：

- 📖 [MULTI-DATASOURCE-GUIDE.md](MULTI-DATASOURCE-GUIDE.md) - 多数据源完整指南
  - MySQL、MongoDB、Redis的详细配置
  - 各数据源的查询语法和示例
  - 所有支持的命令和操作类型
  - 批量跨数据源查询示例
  - 测试数据准备脚本

- ⚡ [REDIS-NATIVE-COMMANDS.md](REDIS-NATIVE-COMMANDS.md) - Redis原生命令支持 ⭐ **新功能**
  - 支持直接输入标准Redis命令（如 `GET user:1`、`SET age 10`）
  - 无需复杂的JSON格式
  - 所有支持的命令列表和示例
  - 使用场景和最佳实践

## 后续优化建议

1. 添加用户认证和权限控制
2. 实现查询语句的安全验证
3. 支持参数化查询
4. 添加查询执行日志记录
5. 实现查询结果分页
6. 支持更多数据源（PostgreSQL、Elasticsearch等）
7. 添加查询执行历史记录
8. 实现异步执行长时间查询
9. 添加查询结果缓存
10. 实现数据源连接池监控

## 许可证

MIT License



