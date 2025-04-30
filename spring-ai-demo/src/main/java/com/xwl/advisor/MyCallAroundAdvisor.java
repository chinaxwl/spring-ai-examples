package com.xwl.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;

/**
 * 使用自定义CallAroundAdvisor在创建ChatClient时添加
 */
public class MyCallAroundAdvisor implements CallAroundAdvisor {
    private static final Logger logger = LoggerFactory.getLogger(MyCallAroundAdvisor.class);

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        logger.info("before: {}", advisedRequest);
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
        logger.info("after: {}", advisedResponse);
        return advisedResponse;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        // 数字越小，越先执行，升序排序
        return 0;
    }
}
