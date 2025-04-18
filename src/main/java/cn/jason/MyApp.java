package cn.jason;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.GetLoadingProgressResponse;
import io.milvus.grpc.MutationResult;

import io.milvus.grpc.SearchResults;
import io.milvus.param.*;
import io.milvus.param.collection.*;
import io.milvus.param.dml.*;
import io.milvus.param.index.*;
import io.milvus.response.SearchResultsWrapper;

import java.util.*;
/**
 * @describe: Milvus 客户端示例程序，演示如何使用 Milvus 进行向量搜索。
 * @Author JasonZhang
 * @Date 2025/4/18
 **/
public class MyApp {

    private static final String COLLECTION_NAME = "test_demo_collection";
    private static final String VECTOR_FIELD = "embedding";
    private static final int VECTOR_DIM = 128;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("1️⃣ 正在连接 Milvus 服务...");
        MilvusServiceClient client = new MilvusServiceClient(
                ConnectParam.newBuilder().withHost("localhost").withPort(19530).build());
        System.out.println("✅ 连接成功");

        checkAndDropOldCollection(client);
        createCollection(client);
        createIndex(client);
        List<List<Float>> vectors = generateRandomVectors(10, VECTOR_DIM);
        insertData(client, vectors);
        flushAndLoad(client);
        searchVectors(client, vectors.get(0));

        client.close();
        System.out.println("✅ 已关闭连接");
    }

    private static void checkAndDropOldCollection(MilvusServiceClient client) {
        System.out.println("2️⃣ 检查集合是否存在...");
        R<Boolean> exists = client.hasCollection(
                HasCollectionParam.newBuilder().withCollectionName(COLLECTION_NAME).build());
        if (exists.getData()) {
            client.dropCollection(DropCollectionParam.newBuilder().withCollectionName(COLLECTION_NAME).build());
            System.out.println("🗑️ 已删除旧集合: " + COLLECTION_NAME);
        } else {
            System.out.println("✅ 集合不存在，跳过删除");
        }
    }

    private static void createCollection(MilvusServiceClient client) {
        System.out.println("3️⃣ 创建新集合...");
        FieldType idField = FieldType.newBuilder()
                .withName("id").withDataType(DataType.Int64).withPrimaryKey(true).withAutoID(true).build();

        FieldType vectorField = FieldType.newBuilder()
                .withName(VECTOR_FIELD).withDataType(DataType.FloatVector).withDimension(VECTOR_DIM).build();

        CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withShardsNum(2)
                .addFieldType(idField)
                .addFieldType(vectorField)
                .build();

        client.createCollection(createCollectionParam);
        System.out.println("✅ 集合创建完成: " + COLLECTION_NAME);
    }

    private static void createIndex(MilvusServiceClient client) {
        System.out.println("4️⃣ 创建向量索引...");
        CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withFieldName(VECTOR_FIELD)
                .withIndexType(IndexType.FLAT)
                .withMetricType(MetricType.L2)
                .withExtraParam("{}")
                .build();

        R<RpcStatus> result = client.createIndex(indexParam);
        System.out.println("✅ 索引创建状态: " + result.getStatus());
    }

    private static List<List<Float>> generateRandomVectors(int count, int dim) {
        System.out.println("5️⃣ 生成随机向量数据...");
        List<List<Float>> vectors = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            List<Float> vector = new ArrayList<>();
            for (int j = 0; j < dim; j++) {
                vector.add(rand.nextFloat());
            }
            vectors.add(vector);
        }
        System.out.println("✅ 已生成向量数量: " + vectors.size());
        return vectors;
    }

    private static void insertData(MilvusServiceClient client, List<List<Float>> vectors) {
        System.out.println("6️⃣ 插入向量数据...");
        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withFields(Collections.singletonList(new InsertParam.Field(VECTOR_FIELD, vectors)))
                .build();

        R<MutationResult> result = client.insert(insertParam);
        System.out.println("✅ 插入成功，行数: " + result.getData().getInsertCnt());
    }

    private static void flushAndLoad(MilvusServiceClient client) throws InterruptedException {
        System.out.println("7️⃣ 正在刷新数据并加载集合...");
        client.flush(FlushParam.newBuilder().withCollectionNames(Collections.singletonList(COLLECTION_NAME)).build());

        client.loadCollection(LoadCollectionParam.newBuilder().withCollectionName(COLLECTION_NAME).build());
        waitForCollectionLoaded(client, COLLECTION_NAME);
    }

    private static void searchVectors(MilvusServiceClient client, List<Float> queryVector) {
        System.out.println("8️⃣ 开始向量搜索...");
        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withVectorFieldName(VECTOR_FIELD)
                .withTopK(3)
                .withVectors(Collections.singletonList(queryVector))
                .withParams("{\"nprobe\": 10}")
                .build();

        R<SearchResults> result = client.search(searchParam);
        SearchResultsWrapper wrapper = new SearchResultsWrapper(result.getData().getResults());

        List<SearchResultsWrapper.IDScore> scores = wrapper.getIDScore(0);
        System.out.println("🔍 搜索结果数量: " + scores.size());

        for (SearchResultsWrapper.IDScore score : scores) {
            System.out.printf("📌 ID = %d, 距离 = %.4f\n", score.getLongID(), score.getScore());
        }
    }

    private static void waitForCollectionLoaded(MilvusServiceClient client, String collectionName) throws InterruptedException {
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
