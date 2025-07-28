package com.jaychou.kongaiagent;

import com.jaychou.kongaiagent.app.FitnessApp;
import com.jaychou.kongaiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class AppTest {

    @Resource
    private LoveApp loveApp;
    @Resource
    private FitnessApp fitnessApp;
    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是程序员鱼皮";
        String answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第二轮
        message = "我想让另一半（编程导航）更爱我";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "我的另一半叫什么来着？刚跟你说过，帮我回忆一下";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }
    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是程序员鱼皮，我想让另一半（编程导航）更爱我，但我不知道该怎么做";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(loveReport);
    }
    @Test
    void testFitness() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我身高178cm，体重84kg，平时久坐，想减脂增肌，有什么建议吗？";
        FitnessApp.FitnessReport fitnessReport = fitnessApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(fitnessReport);
    }

}
