package cn.jason;

import okhttp3.*;
import org.json.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * @describe: 单一职责，生成向量
 * @Author JasonZhang
 * @Date 2025/4/14
**/
public class EmbeddingClient {
    String qwen_api_key = System.getenv("DASHSCOPE_API_KEY");
    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/embeddings";
    private final String API_KEY = qwen_api_key; // ← 替换成你的实际 API Key

    private final OkHttpClient client = new OkHttpClient();

    // 单条文本转向量
    public List<Float> embed(String text) throws IOException {
        return embed(Collections.singletonList(text)).get(0);
    }

    // 多条文本转向量
    public List<List<Float>> embed(List<String> texts) throws IOException {
        JSONObject payload = new JSONObject();
        payload.put("model", "text-embedding-v3");

        JSONArray inputArray = new JSONArray();
        for (String text : texts) {
            inputArray.put(text);
        }
        payload.put("input", inputArray);

        Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(payload.toString(), MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String res = response.body().string();
            JSONObject json = new JSONObject(res);
            JSONArray data = json.getJSONArray("data");

            List<List<Float>> result = new ArrayList<>();
            for (int i = 0; i < data.length(); i++) {
                JSONArray vectorArr = data.getJSONObject(i).getJSONArray("embedding");
                List<Float> vector = new ArrayList<>();
                for (int j = 0; j < vectorArr.length(); j++) {
                    vector.add((float) vectorArr.getDouble(j));
                }
                result.add(vector);
            }
            return result;
        }
    }
}


