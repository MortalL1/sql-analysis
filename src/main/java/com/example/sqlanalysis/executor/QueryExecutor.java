package com.example.sqlanalysis.executor;

import com.example.sqlanalysis.entity.SqlRequest;
import com.example.sqlanalysis.entity.SqlResult;
import org.springframework.beans.factory.InitializingBean;

/**
 * 查询执行器接口
 */
public interface QueryExecutor {
    
    /**
     * 执行查询
     * @param request 请求对象
     * @return 查询结果
     */
    SqlResult execute(SqlRequest request);
    
    /**
     * 判断是否支持该数据源类型
     * @param dataSourceType 数据源类型
     * @return 是否支持
     */
    boolean support(String dataSourceType);
}



