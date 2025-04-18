package cn.jason;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @describe: RAG 应用示例：法律问答机器人。
 * @Author JasonZhang
 * @Date 2025/4/15
 **/
public class RagApp {
    public static void main(String[] args) throws Exception {
        // 构建查询向量
        String query = "我非法得到了公家财物，并转卖给他人，应该触犯了哪条刑法？";
        List<Float> queryVector = EmbeddingClient.embed(query);
        // 向量检索
        List<VectorSearchResult> results = MilvusService.search("law_articles", queryVector);

        // 获取检索结果文本内容
        List<String> topTexts = results.stream()
                .map(VectorSearchResult::text)
                .collect(Collectors.toList());

        // 构建上下文：法律条文拼接
        String context = String.join("\n", topTexts);

        // 构建系统提示 + 提问内容
        String systemPrompt = "请根据以下法律条文内容回答用户问题，并适当结合一些中国相关法律信息最后总结给予建议进行回答：\n" + context;

        // 调用大模型生成回答
        String reply = QwenClient.chat(systemPrompt, query);

        // 打印结果
        System.out.println("🤖 AI 回答：\n" + reply);

        MilvusService.close();
    }
}




