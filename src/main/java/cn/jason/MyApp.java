package cn.jason;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.*;
import io.milvus.param.*;
import io.milvus.param.collection.*;
import io.milvus.param.dml.*;
import io.milvus.param.index.*;
import io.milvus.response.SearchResultsWrapper;

import java.io.IOException;
import java.util.*;
/**
 * @describe: Milvus 客户端示例,主要演示了如何使用 Milvus Java 客户端进行集合的创建、数据的插入和查询。
 * @version 1.0
 * @Author JasonZhang
 * @Date 2025/4/14
**/
public class MyApp {

    private static final String COLLECTION_NAME = "test_demo_collection";
    private static final String VECTOR_FIELD = "embedding";
    private static final int VECTOR_DIM = 1024;

    public static void main(String[] args) throws InterruptedException, IOException {
        // 1. 连接 Milvus
        MilvusServiceClient client = new MilvusServiceClient(
                ConnectParam.newBuilder().withHost("localhost").withPort(19530).build());

        // 2. 删除旧集合（可选）
        R<Boolean> exists = client.hasCollection(HasCollectionParam.newBuilder().withCollectionName(COLLECTION_NAME).build());
        if (exists.getData()) {
            client.dropCollection(DropCollectionParam.newBuilder().withCollectionName(COLLECTION_NAME).build());
            System.out.println("🗑️ 删除旧集合成功");
        }

        // 3. 创建新集合
        FieldType idField = FieldType.newBuilder()
                .withName("id")
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(true)
                .build();

        FieldType vectorField = FieldType.newBuilder()
                .withName(VECTOR_FIELD)
                .withDataType(DataType.FloatVector)
                .withDimension(VECTOR_DIM)
                .build();

        FieldType textField = FieldType.newBuilder()
                .withName("text")
                .withDataType(DataType.VarChar)
                .withMaxLength(512)
                .build();


        CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withShardsNum(2)
                .addFieldType(idField)
                .addFieldType(vectorField)
                .addFieldType(textField)
                .build();

        client.createCollection(createCollectionParam);
        System.out.println("✅ 集合创建成功");

        // 4. 插入 embedding 向量数据
        // 4.1. 实例化你的 embedding client
        EmbeddingClient embeddingClient = new EmbeddingClient();

        // 4.2. 构造多条文本并生成向量
        List<String> texts1 = Arrays.asList(
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

        List<List<Float>> vectors = new ArrayList<>();
        List<String> textList = new ArrayList<>(texts1); // 复制一份文本列表，以便后续插入或返回操作使用。

        String question = "我故意伤害了一个人，但没死亡，应该触犯了哪条刑法";   // 问题向量，通过这个数据去向量库检索
        List<Float> queryVector = embeddingClient.embed(question);
        for (int i = 0; i < texts1.size(); i++) {
            List<Float> vec = embeddingClient.embed(texts1.get(i));
            vectors.add(vec);
        }

        // 4.3. 插入真实向量数据
        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withFields(Arrays.asList(
                        new InsertParam.Field(VECTOR_FIELD, vectors),
                        new InsertParam.Field("text", textList)
                ))
                .build();

        R<MutationResult> insertResult = client.insert(insertParam);
        System.out.println("✅ 插入数据行数: " + insertResult.getData().getInsertCnt());


        // 5. 强制 flush，确保可见
        client.flush(FlushParam.newBuilder().withCollectionNames(Collections.singletonList(COLLECTION_NAME)).build());

        // 6. 创建索引（在 flush 后）
        CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withFieldName(VECTOR_FIELD)
                .withIndexType(IndexType.FLAT)
                .withMetricType(MetricType.L2)
                .withExtraParam("{}")
                .build();

        R<RpcStatus> indexResult = client.createIndex(indexParam);
        System.out.println("✅ 索引创建结果: " + indexResult.getStatus());

        // 7. 加载集合
        client.loadCollection(LoadCollectionParam.newBuilder().withCollectionName(COLLECTION_NAME).build());

        waitForCollectionLoaded(client, COLLECTION_NAME);

        // 8. 检索向量
        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withMetricType(MetricType.L2)
                .withOutFields(Arrays.asList("id", "text")) // 取出原始文本字段
                .withTopK(3)
                .withVectors(Collections.singletonList(queryVector))
                .withVectorFieldName(VECTOR_FIELD)
                .withParams("{\"nprobe\": 10}")
                .build();

        R<SearchResults> search = client.search(searchParam);
        SearchResultsWrapper wrapper = new SearchResultsWrapper(search.getData().getResults());

        List<SearchResultsWrapper.IDScore> scores = wrapper.getIDScore(0);
        List<String> texts2 = (List<String>) wrapper.getFieldWrapper("text").getFieldData(); // 所有检索到的 text 字段值

        System.out.println("🔍 检索结果数量：" + scores.size());

        for (int i = 0; i < scores.size(); i++) {
            long id = scores.get(i).getLongID();
            float score = scores.get(i).getScore();
            String text = texts2.get(i); // 一一对应
            System.out.printf("📌 ID = %d, 距离 = %.4f, 文本 = %s\n", id, score, text);
        }

        System.out.println("✅ 检索流程结束");
        client.close();
    }

    public static void waitForCollectionLoaded(MilvusServiceClient client, String collectionName) throws InterruptedException {
        for (int i = 0; i < 30; i++) {
            R<GetLoadingProgressResponse> progress = client.getLoadingProgress(
                    GetLoadingProgressParam.newBuilder().withCollectionName(collectionName).build());
            if (progress.getData().getProgress() == 100) {
                System.out.println("📦 集合加载完成！");
                return;
            }
            Thread.sleep(200);
        }
        throw new RuntimeException("❌ 集合加载超时！");
    }
}
