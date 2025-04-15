package cn.jason;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
/**
 * @describe: 阿里云Qwen大模型客户端
 * @Author JasonZhang
 * @Date 2025/4/15
**/
public class QwenClient {
    String qwen_api_key = System.getenv("DASHSCOPE_API_KEY");
    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private final String API_KEY = qwen_api_key; // 换成你的 API Key

    private final OkHttpClient client = new OkHttpClient();

    public String chat(String userInput) throws IOException {
        JSONObject payload = new JSONObject();
        payload.put("model", "qwen-turbo");

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "user").put("content", userInput));
        payload.put("input", new JSONObject().put("messages", messages));
        payload.put("parameters", new JSONObject().put("temperature", 0.7));

        Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(payload.toString(), MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String res = response.body().string();
            JSONObject json = new JSONObject(res);
            return json.getJSONObject("output").getString("text");
        }
    }
}

