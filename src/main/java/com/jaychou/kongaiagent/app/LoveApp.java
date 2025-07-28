package com.jaychou.kongaiagent.app;

import com.jaychou.kongaiagent.chatmemory.FileBasedChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import com.jaychou.kongaiagent.advisor.MyLoggerAdvisor;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * ClassName: LoveApp
 * Package: com.jaychou.kongaiagent.app
 * Description:
 *
 * @Author: 红模仿
 * @Create: 2025/5/23 - 13:59
 * @Version: v1.0
 */
@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";
    public record LoveReport(String title, List<String> suggestions) {
    }

    public LoveApp(ChatModel dashscopeChatModel) {
        // 初始化基于文件的对话记忆
        String fileDir = System.getProperty("user.dir") + "/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory)
                )
                .build();
    }

    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
    public LoveReport doChatWithReport(String message, String chatId) {
        // 发起一次聊天请求，结果封装为 LoveReport 对象
        LoveReport loveReport = chatClient
                .prompt() // 开始一个 prompt 构造器，链式调用
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                // 设置系统提示词，通常用于指定对话背景、风格或目标
                .user(message) // 设置用户发来的消息内容
                .advisors(spec -> spec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId) // 指定对话 ID，用于保持上下文
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))     // 上下文记忆的最大条数
                .call() // 发起调用，开始与语言模型交互
                .entity(LoveReport.class); // 将响应解析为 LoveReport 类型的对象

        log.info("loveReport: {}", loveReport); // 打印返回的结果，方便调试
        return loveReport; // 返回结果
    }



}

