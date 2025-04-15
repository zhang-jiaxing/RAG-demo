package cn.jason;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @describe: RAG 应用示例：法律问答机器人。
 * @Author JasonZhang
 * @Date 2025/4/15
**/
public class RagApp {
    public static void main(String[] args) throws IOException {
        // 模拟文本数据
        List<String> texts = Arrays.asList(
                "中华人民共和国刑法（第232条）: 故意杀人的，处死刑、无期徒刑或者十年以上有期徒刑；情节较轻的，处三年以上十年以下有期徒刑。",
                "中华人民共和国刑法（第264条）: 盗窃公私财物，数额较大的，或者多次盗窃、入户盗窃、携带凶器盗窃、扒窃的，处三年以下有期徒刑、拘役或者管制，并处或者单处罚金；数额巨大或者有其他严重情节的，处三年以上十年以下有期徒刑，并处罚金；数额特别巨大或者有其他特别严重情节的，处十年以上有期徒刑或者无期徒刑，并处罚金或者没收财产。",
                "中华人民共和国刑法（第133条）: 在道路上驾驶机动车，有下列情形之一的，处拘役，并处罚金：追逐竞驶、醉驾、严重超载超速、非法运输危险化学品等。",
                "中华人民共和国刑法（第382条）: 国家工作人员利用职务上的便利，侵吞、窃取、骗取或者以其他手段非法占有公共财物的，是贪污罪。",
                "中华人民共和国刑法（第266条）: 诈骗公私财物，数额较大的，处三年以下有期徒刑、拘役或者管制，并处或者单处罚金；数额巨大或者有其他严重情节的，处三年以上十年以下有期徒刑，并处罚金；数额特别巨大或者有其他特别严重情节的，处十年以上有期徒刑或者无期徒刑，并处罚金或者没收财产。",
                "中华人民共和国刑法（第234条）: 故意伤害他人身体的，处三年以下有期徒刑、拘役或者管制。致人重伤的，处三年以上十年以下有期徒刑；致人死亡或造成严重残疾的，处十年以上有期徒刑、无期徒刑或者死刑。",
                "中华人民共和国刑法（第130条）: 非法持有枪支、弹药、爆炸物的，构成犯罪的，依法追究刑事责任。",
                "中华人民共和国刑法（第310条）: 窝藏、包庇犯罪分子的，处三年以下有期徒刑、拘役或者管制；情节严重的，处三年以上七年以下有期徒刑。",
                "中华人民共和国刑法（第287条）: 利用计算机信息网络实施诈骗、盗窃、敲诈勒索的，依法追究刑事责任。",
                "中华人民共和国刑法（第299条）: 冒充国家机关工作人员招摇撞骗的，处三年以下有期徒刑、拘役、管制或者罚金。"
        );

        EmbeddingClient Eclient = new EmbeddingClient();
        MilvusService milvusService = new MilvusService();
        QwenClient qwen = new QwenClient();

        // ✅ 1. 向量化 + 存入 Milvus
        List<List<Float>> vectors = Eclient.embed(texts);
        milvusService.createCollectionIfNotExists("law_articles",1024);// 创建集合，维度为1024
        milvusService.insert("law_articles", texts, vectors);
        milvusService.createBasicIndex("law_articles","embedding");
        milvusService.loadCollection("law_articles");
        // ✅ 2. 用户提问+ 向量化 + 检索结果A
        String query = "我故意伤害了一个人，但没死亡，应该触犯了哪条刑法";
        List<Float> queryVector = Eclient.embed(query);
        List<VectorSearchResult> results = milvusService.search("law_articles", queryVector);

        for (VectorSearchResult result : results) {
            System.out.println("🎯 匹配结果:");
            System.out.println("ID: " + result.id());
            System.out.println("相似度得分: " + result.score());
            System.out.println("原文: " + result.text());
            System.out.println("----------");
        }

        // ✅ 3. 构建 prompt + 向通义千问提问
        // 获取 top K 检索结果文本
        List<String> topTexts = results.stream()
                .map(VectorSearchResult::text)
                .collect(Collectors.toList());

        // 构建 context 上下文内容
        String context = String.join("\n", topTexts);

        // 构建 prompt
        String prompt = "请根据以下法律条文内容回答用户问题，不要编造，仅基于提供的信息进行回答：\n" +
                context +
                "\n\n问题：" + query +
                "\n请用简洁准确的法律术语进行回答。";

        // 调用通义千问 API 生成回答
        String reply = qwen.chat(prompt);
        System.out.println("🤖 AI 回答：\n" + reply);

    }
}




