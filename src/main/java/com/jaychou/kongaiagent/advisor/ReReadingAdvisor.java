package com.jaychou.kongaiagent.advisor;

import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义 Re2 Advisor
 *
 * 📌 作用：
 * 通过在用户输入前增加“重新阅读问题”的提示，
 * 引导大语言模型（如 ChatGPT、LLM）更认真、重复地理解问题，
 * 以提升模型在复杂问题场景下的推理能力与准确率。
 *
 * ✅ 同时支持同步调用（aroundCall）和流式调用（aroundStream）。
 */
public class ReReadingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    /**
     * 在请求发送前，对原始用户请求进行包装：
     * - 将用户原始输入提取为参数 re2_input_query；
     * - 修改 userText，在其中多加一句：
     *   “Read the question again: {原始内容}”
     *
     * 这相当于 Prompt 工程中的一种“提示增强技术”。
     *
     * @param advisedRequest 原始请求
     * @return 包装后的请求
     */
    private AdvisedRequest before(AdvisedRequest advisedRequest) {
        // 复制用户参数并注入 re2_input_query 字段
        Map<String, Object> advisedUserParams = new HashMap<>(advisedRequest.userParams());
        advisedUserParams.put("re2_input_query", advisedRequest.userText());

        // 构造新的 userText（提示增强）并构建新请求对象
        return AdvisedRequest.from(advisedRequest)
                .userText("""
                        {re2_input_query}
                        Read the question again: {re2_input_query}
                        """)
                .userParams(advisedUserParams)
                .build();
    }

    /**
     * 同步调用场景下的处理逻辑。
     * 在调用链执行前，先包装请求内容，再继续处理。
     *
     * @param advisedRequest 用户请求
     * @param chain 调用链
     * @return 响应结果
     */
    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        return chain.nextAroundCall(this.before(advisedRequest));
    }
//    @Override
//    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
//    // 更新上下文
//    advisedRequest = advisedRequest.updateContext(context -> {
//        context.put("key", "value");
//        return context;
//    });
//
//    // 读取上下文
//    Object value = advisedResponse.adviseContext().get("key");

//        return Mono.just(advisedRequest)  // 1. 将请求对象封装成 Mono（单个元素的响应式流）
//                .publishOn(Schedulers.boundedElastic()) // 2. 切换线程，使用弹性线程池处理，适合阻塞或耗时操作
//                .map(request -> {
//                    // 3. 请求前处理逻辑，比如修改请求参数
//                    return this.before(request);
//                })
//                .flatMapMany(request -> chain.nextAroundStream(request))
//                // 4. 调用后续链条中的下一个 StreamAroundAdvisor，返回 Flux<AdvisedResponse>（响应式流）
//                .map(response -> {
//                    // 5. 对响应进行处理，比如修改响应数据
//                    return modifyResponse(response);
//                });
//    }

    /**
     * 流式调用场景下的处理逻辑。
     * 同样地，先增强 Prompt，再进入流式处理链。
     *
     * @param advisedRequest 用户请求
     * @param chain 流式调用链
     * @return Flux 形式的响应流
     */
    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {

        return chain.nextAroundStream(this.before(advisedRequest));
    }

    /**
     * 设置优先级，0 表示默认优先级。
     */
    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 返回当前 Advisor 的名称（类名）
     */
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}

