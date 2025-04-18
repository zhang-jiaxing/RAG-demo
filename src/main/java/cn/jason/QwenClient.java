package cn.jason;

import com.alibaba.dashscope.aigc.generation.*;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * @describe: 千问大模型客户端（DashScope SDK）
 * @Author JasonZhang
 * @Date 2025/4/18
 */
@Slf4j
public class QwenClient {

    private static final String API_KEY = System.getenv("DASHSCOPE_API_KEY");
    private static final String MODEL_NAME = "qwen-turbo"; // 可切换其他模型

    /**
     * 与模型对话
     * @param userInput 用户输入的问题
     * @param context 可选的系统提示（用于指令设定）
     * @return 模型返回的答案
     */
    public static String chat(String context, String userInput) throws ApiException, NoApiKeyException, InputRequiredException {
        // 构造消息
        // 系统提示（System Prompt）
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content(context) // 例如："你是一个法律顾问，回答问题要严谨、专业。"
                .build();

        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(userInput)
                .build();

        // 构造请求参数
        GenerationParam param = GenerationParam.builder()
                .apiKey(API_KEY)
                .model(MODEL_NAME)
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();

        GenerationResult result = new Generation().call(param);
        StringBuilder responseBuilder = new StringBuilder();

        // 提取回答
        GenerationOutput output = result.getOutput();
        if (output != null && output.getChoices() != null) {
            output.getChoices().forEach(choice -> {
                Message message = choice.getMessage();
                if (message != null && message.getContent() != null) {
                    responseBuilder.append(message.getContent()).append("\n");
                }
            });
        } else {
            log.warn("模型未返回有效 choices：{}", result);
            return "模型未能返回回答，请稍后再试。";
        }

        return responseBuilder.toString().trim(); // 去除结尾多余换行
    }

    public static void main(String[] args) throws Exception {
        String question = "我非法得到了公家财物，并转卖给他人，应该触犯了哪条刑法？";
        String response = chat("", question);// 传入空字符串，不使用系统提示
        System.out.println("模型回答：\n" + response);
    }
}
