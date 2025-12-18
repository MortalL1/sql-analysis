package com.example.sqlanalysis.enums;

/**
 * 数据源类型枚举
 */
public enum DataSourceType {
    
    /**
     * MySQL数据库
     */
    MYSQL("mysql", "MySQL数据库"),
    
    /**
     * MongoDB数据库
     */
    MONGODB("mongodb", "MongoDB数据库"),
    
    /**
     * Redis缓存
     */
    REDIS("redis", "Redis缓存");
    
    private final String code;
    private final String description;
    
    DataSourceType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据code获取枚举
     */
    public static DataSourceType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return MYSQL; // 默认MySQL
        }
        
        for (DataSourceType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        
        throw new IllegalArgumentException("不支持的数据源类型: " + code);
    }
}



