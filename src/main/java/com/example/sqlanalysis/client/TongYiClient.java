package com.example.sqlanalysis.client;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.springframework.stereotype.Component;


import java.util.Arrays;

@Component
public class TongYiClient {
    private static final String apiKey = "sk-c978d65cd9c544d48d9f6d01182e7638";


    public static GenerationResult callWithMessage(String type, String content) throws ApiException {
        Generation gen = new Generation();

        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("You are an expert proficient in " + type)
                .build();

        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(content)
                .build();

        GenerationParam param = GenerationParam.builder()
                .apiKey(apiKey)
                .model("qwen-flash")
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
        GenerationResult call = null;
        try {
            call = gen.call(param);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return call;
    }

}

