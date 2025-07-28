package com.jaychou.kongaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

/**
 * 自定义日志增强顾问类
 * 实现CallAroundAdvisor和StreamAroundAdvisor接口，用于在AI请求和响应过程中记录日志
 * 主要功能：
 * 1. 在AI处理请求前记录用户输入的提示词
 * 2. 在AI生成响应后记录AI的回复文本
 * 3. 支持同步调用和流式调用两种场景的日志记录
 */
/**
 * 自定义日志 Advisor，用于统一记录 AI 请求与响应日志。
 * 实现了 CallAroundAdvisor 和 StreamAroundAdvisor，
 * 能处理同步和流式请求场景，记录 info 级别的日志信息。
 */

@Slf4j
public class MyLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    /**
     * 返回 Advisor 的名称（类名）
     */
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 设置 Advisor 的执行顺序。值越小优先级越高。
     * 这里设置为 0 表示较高优先级。
     */
    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 请求处理前的操作。
     * 打印用户输入的提示词。
     *
     * @param request 用户请求体
     * @return 处理后的请求体（本例中未修改）
     */
    private AdvisedRequest before(AdvisedRequest request) {
        log.info("AI Request: {}", request.userText());
        return request;
    }

    /**
     * 响应处理后的操作。
     * 打印 AI 返回的文本内容。
     *
     * @param advisedResponse AI 处理后的响应结果
     */
    private void observeAfter(AdvisedResponse advisedResponse) {
        log.info("AI Response: {}", 
                 advisedResponse.response().getResult().getOutput().getText());
    }

    /**
     * 同步调用处理（用于非流式响应，例如一次请求一次响应）。
     * 在请求前和响应后分别插入日志打印逻辑。
     *
     * @param advisedRequest 请求参数
     * @param chain 调用链，用于继续后续处理
     * @return 最终响应
     */
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        // 请求前日志记录
        advisedRequest = this.before(advisedRequest);

        // 调用下一个处理器（如真正的 AI 服务）
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);

        // 响应后日志记录
        this.observeAfter(advisedResponse);

        return advisedResponse;
    }

    /**
     * 流式调用处理（用于 AI 输出是逐段生成的场景，如聊天流式回复）。
     * 会聚合所有流式片段后打印一次完整的 AI 响应。
     *
     * @param advisedRequest 请求参数
     * @param chain 流式调用链
     * @return 聚合后的流式响应 Flux
     */
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        // 请求前日志记录
        advisedRequest = this.before(advisedRequest);

        // 获取流式响应 Flux
        Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);

        // 聚合流并在完成后统一记录响应日志
        return (new MessageAggregator()).aggregateAdvisedResponse(advisedResponses, this::observeAfter);
    }
}
