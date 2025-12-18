# 列顺序修复说明

## 问题描述

之前在Web界面显示查询结果时，表格列的顺序是随机的，不符合数据库的实际列顺序。

例如：
- Redis的 `GET` 命令应该先显示 `key`，再显示 `value`
- MySQL查询应该按照SELECT语句中的列顺序显示
- MongoDB查询应该按照文档字段的定义顺序显示

## 原因分析

### 后端问题
在Java中使用 `HashMap` 存储数据行时，`HashMap` 不保证键的顺序，导致列顺序混乱。

### 前端问题
使用 `Object.keys()` 获取列名时，JavaScript对象的属性顺序不可靠，没有使用后端返回的 `columns` 字段。

## 解决方案

### 1. 后端修改

将所有执行器中的 `HashMap` 替换为 `LinkedHashMap`：

#### RedisExecutor.java
```java
// 修改前
Map<String, Object> data = new HashMap<>();
data.put("key", key);
data.put("value", value);

// 修改后
Map<String, Object> data = new LinkedHashMap<>();
data.put("key", key);
data.put("value", value);
```

`LinkedHashMap` 会保持键的插入顺序，确保 `key` 始终在 `value` 之前。

#### MySQLExecutor.java
```java
// 修改前
Map<String, Object> row = new HashMap<>();

// 修改后
Map<String, Object> row = new LinkedHashMap<>();
```

按照ResultSet的列顺序插入数据，保持SQL查询的列顺序。

#### MongoDBExecutor.java
```java
// 修改前
Map<String, Object> row = new HashMap<>(doc);

// 修改后
Map<String, Object> row = new LinkedHashMap<>(doc);
```

Document本身就保持插入顺序，使用LinkedHashMap进一步确保顺序不变。

### 2. 前端修改

#### sql-query-ui.html

**添加全局变量保存列顺序**：
```javascript
let currentColumns = [];  // 保存列的顺序
```

**使用后端返回的 columns 字段**：
```javascript
// 修改前
const columns = Object.keys(currentData[0]);

// 修改后
currentColumns = data.columns && data.columns.length > 0 
    ? data.columns 
    : Object.keys(data.data[0]);
```

**在渲染表格时使用保存的列顺序**：
```javascript
function renderTable() {
    const columns = currentColumns;  // 使用保存的列顺序
    // ... 渲染逻辑
}
```

## 修改的文件

### 后端（Java）
1. ✅ `MySQLExecutor.java` - 查询结果使用LinkedHashMap
2. ✅ `MongoDBExecutor.java` - 文档转换使用LinkedHashMap
3. ✅ `RedisExecutor.java` - 所有命令结果使用LinkedHashMap
   - executeGet()
   - executeExists()
   - executeKeys()
   - executeHashGet()
   - executeHashGetAll()
   - executeListRange()
   - executeSetMembers()
   - executeTTL()

### 前端（HTML/JavaScript）
1. ✅ `sql-query-ui.html` - 使用后端返回的列顺序

## 测试验证

### Redis测试
```bash
# GET命令 - 应该显示：key | value
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{"dataSourceType": "redis", "sql": "GET user:1"}'

期望列顺序：key, value
```

### MySQL测试
```bash
# SELECT查询 - 应该按照SELECT语句的列顺序
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{"dataSourceType": "mysql", "sql": "SELECT name, age, email FROM user"}'

期望列顺序：name, age, email
```

### MongoDB测试
```bash
# find查询 - 应该按照文档字段的定义顺序
curl -X POST http://localhost:8080/api/sql/execute \
  -H "Content-Type: application/json" \
  -d '{"dataSourceType": "mongodb", "sql": "{\"collection\": \"users\", \"operation\": \"find\"}"}'

期望列顺序：_id, name, age, email（按文档插入顺序）
```

## 技术细节

### LinkedHashMap vs HashMap

| 特性 | HashMap | LinkedHashMap |
|------|---------|---------------|
| 插入顺序 | ❌ 不保证 | ✅ 保证 |
| 性能 | 稍快 | 稍慢（但差异很小） |
| 内存 | 稍少 | 稍多（需要维护链表） |
| 适用场景 | 不关心顺序 | 需要保持顺序 |

对于我们的场景，列顺序非常重要，使用 `LinkedHashMap` 的性能开销完全可以接受。

### JavaScript对象顺序

虽然ES6+规范中对象属性有一定的顺序保证，但：
1. 不同浏览器实现可能不同
2. 不如显式使用数组保存顺序可靠
3. 后端已经提供了 `columns` 字段，应该优先使用

## 效果对比

### 修改前
```
Redis GET命令可能显示：
value | key  ❌

MySQL查询可能显示：
email | age | name  ❌
```

### 修改后
```
Redis GET命令始终显示：
key | value  ✅

MySQL查询按SELECT顺序显示：
name | age | email  ✅
```

## 总结

通过在后端使用 `LinkedHashMap` 保持数据插入顺序，并在前端使用后端返回的 `columns` 字段，我们确保了：

1. ✅ 表格列的显示顺序与数据库定义一致
2. ✅ Redis命令结果按照逻辑顺序显示（key在前，value在后）
3. ✅ MySQL查询结果按照SELECT语句的列顺序显示
4. ✅ MongoDB查询结果按照文档字段的定义顺序显示
5. ✅ 跨浏览器的一致性体验

现在用户在Web界面看到的表格列顺序，与在数据库客户端工具中看到的完全一致！🎉



