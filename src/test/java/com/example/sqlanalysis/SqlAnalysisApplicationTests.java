package com.example.sqlanalysis;

import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.fastjson.JSON;
import com.example.sqlanalysis.client.TongYiClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class SqlAnalysisApplicationTests {
    @Resource
    private TongYiClient tongYiClient;

    @Test
    void contextLoads() throws NoApiKeyException, InputRequiredException {
        GenerationResult mysql = TongYiClient.callWithMessage("mysql", "SQL执行失败: SQL执行失败: PreparedStatementCallback; bad SQL grammar []; nested exception is java.sql.SQLSyntaxErrorException: Table 'test.user' doesn't exist\n");
        System.out.println(JSON.toJSONString(mysql));
    }

}
