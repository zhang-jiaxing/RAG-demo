package cn.jason;

import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
/**
 * @describe: 单一职责，生成向量
 * @Author JasonZhang
 * @Date 2025/4/14
 **/
public class EmbeddingClient {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingClient.class);

    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/embeddings";
    private static final String API_KEY = System.getenv("DASHSCOPE_API_KEY");

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public EmbeddingClient() {
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new RuntimeException("❌ 未检测到 DASHSCOPE_API_KEY，请设置环境变量");
        }
        logger.info("✅ 成功获取 API_KEY，长度: {}", API_KEY.length());
    }

    /**
     * 单条文本向量化
     */
    public static List<Float> embed(String text) throws Exception {
        List<List<Float>> results = embed(Collections.singletonList(text));
        return results.isEmpty() ? Collections.emptyList() : results.get(0);
    }

    /**
     * 多条文本向量化
     */
    public static List<List<Float>> embed(List<String> texts) throws Exception {
        logger.info("📨 准备向量化文本，共计: {} 条", texts.size());

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "text-embedding-v3");
        requestBody.put("input", new JSONArray(texts));
        requestBody.put("dimensions", 1024);
        requestBody.put("encoding_format", "float");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            logger.error("❌ 向量化请求失败，状态码: {}, 响应体: {}", response.statusCode(), response.body());
            throw new RuntimeException("向量请求失败: " + response.statusCode());
        }

        String responseBody = response.body();
        logger.info("✅ 请求成功，响应预览: {}", responseBody.length() > 60 ? responseBody.substring(0, 60) + "..." : responseBody);

        return extractEmbeddings(responseBody);
    }

    /**
     * 从响应中提取向量
     */
    private static List<List<Float>> extractEmbeddings(String json) {
        logger.info("🔍 正在提取向量...");
        List<List<Float>> vectors = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(json);
        JSONArray dataArray = jsonObject.getJSONArray("data");

        for (int i = 0; i < dataArray.length(); i++) {
            JSONArray vectorArray = dataArray.getJSONObject(i).getJSONArray("embedding");
            List<Float> vector = new ArrayList<>();
            for (int j = 0; j < vectorArray.length(); j++) {
                vector.add((float) vectorArray.getDouble(j));
            }
            vectors.add(vector);
        }

        logger.info("📦 提取完成，共计向量条数: {}", vectors.size());
        return vectors;
    }

    /**
     * 示例主方法（可删除）
     */
    public static void main(String[] args) throws Exception {
        List<String> inputs = Arrays.asList(
                "中华人民共和国刑法（第232条）：故意杀人的，处死刑、无期徒刑或者十年以上有期徒刑。",
                "中华人民共和国民法典（第123条）：侵权责任法是调整侵权行为所产生的法律关系的法律。",
                "中华人民共和国公司法（第45条）：股东会是公司的最高权力机构。"
        );
        List<List<Float>> vectors = embed(inputs);
        System.out.println("✔ 向量维度：" + vectors.get(0).size());
    }
}


