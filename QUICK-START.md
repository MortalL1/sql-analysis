# 快速开始指南

## 🚀 5分钟快速启动

### 第一步：配置数据库连接

编辑 `src/main/resources/application.properties`：

```properties
# MySQL配置（必需）
spring.datasource.url=jdbc:mysql://localhost:3306/test
spring.datasource.username=root
spring.datasource.password=你的密码

# MongoDB配置（可选）
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=test

# Redis配置（可选）
spring.redis.host=localhost
spring.redis.port=6379
```

### 第二步：准备测试数据（MySQL）

连接到MySQL，执行以下SQL：

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS test DEFAULT CHARACTER SET utf8mb4;

-- 使用数据库
USE test;

-- 创建测试表
CREATE TABLE IF NOT EXISTS user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    age INT,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入测试数据
INSERT INTO user (name, age, email) VALUES
('张三', 25, 'zhangsan@example.com'),
('李四', 30, 'lisi@example.com'),
('王五', 28, 'wangwu@example.com');
```

### 第三步：启动应用

```bash
cd /Users/lwj/work/sql-analysis
mvn spring-boot:run
```

等待看到以下提示：
```
Started SqlAnalysisApplication in X.XXX seconds
```

### 第四步：打开Web界面

双击打开 `sql-query-ui.html` 文件

### 第五步：执行第一个查询

1. 在Web界面中，数据源选择 `MySQL`
2. 输入SQL：`SELECT * FROM user`
3. 点击"执行查询"按钮
4. 查看结果！

## ✅ 验证安装

### 方法1：使用Web界面

1. 打开 `sql-query-ui.html`
2. 执行查询：`SELECT * FROM user`
3. 应该看到3条测试数据

### 方法2：使用curl命令

```bash
# 健康检查
curl http://localhost:8080/api/sql/health

# 执行查询
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{
    "dataSourceType": "mysql",
    "sql": "SELECT * FROM user"
  }'
```

## 🎯 下一步

### 探索更多功能

1. **MongoDB查询**
   - 参考：[MULTI-DATASOURCE-GUIDE.md](MULTI-DATASOURCE-GUIDE.md)
   - 示例：`{"collection": "users", "operation": "find"}`

2. **Redis命令**
   - 参考：[MULTI-DATASOURCE-GUIDE.md](MULTI-DATASOURCE-GUIDE.md)
   - 示例：`{"command": "GET", "key": "user:1"}`

3. **批量查询**
   - 一次执行多个数据源的查询
   - 支持混合查询

### 了解更多

- 📖 [README.md](README.md) - 完整文档
- 🌐 [WEB-UI-GUIDE.md](WEB-UI-GUIDE.md) - Web界面详细说明
- 📝 [test-examples.md](test-examples.md) - 更多测试示例

## ❓ 常见问题

### Q1: 启动时报错 "Cannot load driver class: com.mysql.cj.jdbc.Driver"

**解决方案**：
```bash
mvn clean install
mvn spring-boot:run
```

### Q2: 插入中文数据报错

**解决方案**：
参考主README中的"字符集配置"章节，确保MySQL使用 `utf8mb4` 字符集。

### Q3: Web界面无法连接后端

**检查清单**：
- ✅ 后端是否启动（端口8080）
- ✅ 浏览器控制台是否有错误
- ✅ 防火墙是否阻止

### Q4: MongoDB/Redis查询失败

**原因**：
- MongoDB或Redis服务未启动
- 配置文件中的连接信息不正确

**解决方案**：
1. 启动对应的服务
2. 检查配置文件中的连接信息

## 🎉 成功了！

如果你能看到查询结果，恭喜你已经成功运行了SQL多数据源查询系统！

现在你可以：
- ✨ 查询MySQL数据库
- ✨ 查询MongoDB文档
- ✨ 查询Redis缓存
- ✨ 使用美观的Web界面
- ✨ 查看详细的执行统计

享受使用吧！🚀

---

**需要帮助？**
- 查看详细文档：[README.md](README.md)
- 多数据源指南：[MULTI-DATASOURCE-GUIDE.md](MULTI-DATASOURCE-GUIDE.md)
- Web界面指南：[WEB-UI-GUIDE.md](WEB-UI-GUIDE.md)



