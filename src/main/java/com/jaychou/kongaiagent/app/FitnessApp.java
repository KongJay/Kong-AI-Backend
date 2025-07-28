package com.jaychou.kongaiagent.app;

import com.jaychou.kongaiagent.chatmemory.FileBasedChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * ClassName: FitnessApp
 * Package: com.jaychou.kongaiagent.app
 * Description:
 *
 * @Author: 红模仿
 * @Create: 2025/7/28 - 17:10
 * @Version: v1.0
 */
@Component
@Slf4j
public class FitnessApp {
    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT =
            "扮演一位专业的私人健身教练，具备运动科学与营养知识。" +
                    "开场向用户表明身份，告知用户可以咨询任何与健身相关的问题，如增肌、减脂、塑形、体能提升等。" +
                    "围绕三种常见状态提问：初学者请说明身体状况、运动经验和目标；" +
                    "有一定经验者请说明当前训练计划、瓶颈问题和饮食结构；" +
                    "进阶者请提供训练周期安排、康复计划或特殊目标。" +
                    "引导用户描述具体情况，包括身体指标、训练频率、饮食习惯、受伤历史等，以便制定个性化的健身建议与计划。";

    public record FitnessReport(String title, List<String> suggestions, Double bmi, String goal) {
    }


    public FitnessApp(ChatModel dashscopeChatModel) {
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
    public FitnessReport doChatWithReport(String message, String chatId) {
        // 发起一次聊天请求，结果封装为 FitnessReport 对象
        FitnessReport fitnessReport = chatClient
                .prompt() // 开始一个 prompt 构造器，链式调用
                .system(SYSTEM_PROMPT + "每次对话后都要生成结果，标题为{用户名}的健身建议报告，内容包括建议列表、BMI 和健身目标")
                // 设置系统提示词：构建 AI 角色背景、风格和行为目标
                .user(message) // 设置用户发来的消息内容
                .advisors(spec -> spec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId) // 绑定 chatId，用于上下文记忆
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))     // 设置上下文记忆的历史条数
                .call() // 发起调用，开始与语言模型交互
                .entity(FitnessReport.class); // 将响应解析为 FitnessReport 类型的对象

        log.info("fitnessReport: {}", fitnessReport); // 打印健身报告返回结果，便于调试
        return fitnessReport; // 返回生成的健身报告
    }

}
