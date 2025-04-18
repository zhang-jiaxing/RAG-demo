package cn.jason;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.SearchResults;
import io.milvus.param.*;
import io.milvus.param.collection.*;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.SearchResultsWrapper;

import java.util.*;

/**
 * @describe: 封装了 Milvus 的基本操作，简化使用
 * @Author JasonZhang
 * @Date 2025/4/14
 */
public class MilvusService {
    private static MilvusServiceClient client;

    /**
     * 静态初始化块确保 client 在类加载时初始化
     */
    static {
        client = new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withHost("localhost")
                        .withPort(19530)
                        .build()
        );
        System.out.println("🟢 Milvus 客户端已初始化");
    }

    /**
     * @param collectionName 集合名称
     * @param texts          文本数据列表
     * @param vectors        向量数据列表
     *                       插入数据（基础版本，不自动刷新和加载集合）
     */
    public static void insert(String collectionName, List<String> texts, List<List<Float>> vectors) {
        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(collectionName)
                .withFields(Arrays.asList(
                        new InsertParam.Field("embedding", vectors),
                        new InsertParam.Field("text", texts)
                ))
                .build();

        R<MutationResult> insertResult = client.insert(insertParam);
        System.out.println("✅ 插入数据行数: " + insertResult.getData().getInsertCnt());
    }

    /**
     * @param collectionName 集合名称
     * @param texts          文本数据列表
     * @param vectors        向量数据列表
     *                       插入数据并刷新 + 加载集合（推荐使用）
     */
    public static long insertAndPrepare(String collectionName, List<String> texts, List<List<Float>> vectors) {
        System.out.println("📥 正在插入数据到集合: " + collectionName + "，共 " + texts.size() + " 条");

        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(collectionName)
                .withFields(Arrays.asList(
                        new InsertParam.Field("embedding", vectors),
                        new InsertParam.Field("text", texts)
                ))
                .build();

        R<MutationResult> insertResult = client.insert(insertParam);
        long insertCount = insertResult.getData().getInsertCnt();
        System.out.println("✅ 插入成功: " + insertCount + " 条数据");

        // 刷新集合
        FlushParam flushParam = FlushParam.newBuilder()
                .withCollectionNames(Collections.singletonList(collectionName))
                .build();
        client.flush(flushParam);
        System.out.println("🔄 集合已刷新: " + collectionName);

        // 加载集合
        LoadCollectionParam loadParam = LoadCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build();
        client.loadCollection(loadParam);
        System.out.println("📦 集合已加载进内存: " + collectionName);

        return insertCount;
    }

    /**
     * @param queryVector    查询向量
     * @param collectionName 集合名称
     *                       检索向量，返回 Top K 匹配结果
     */
    public static List<VectorSearchResult> search(String collectionName, List<Float> queryVector) {
        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName(collectionName)
                .withMetricType(MetricType.L2)
                .withOutFields(Arrays.asList("id", "text"))
                .withTopK(3)
                .withVectors(Collections.singletonList(queryVector))
                .withVectorFieldName("embedding")
                .withParams("{\"nprobe\": 10}")
                .build();

        R<SearchResults> search = client.search(searchParam);
        SearchResultsWrapper wrapper = new SearchResultsWrapper(search.getData().getResults());

        List<SearchResultsWrapper.IDScore> scores = wrapper.getIDScore(0);
        List<String> texts = (List<String>) wrapper.getFieldWrapper("text").getFieldData();

        List<VectorSearchResult> results = new ArrayList<>();
        for (int i = 0; i < scores.size(); i++) {
            long id = scores.get(i).getLongID();
            float score = scores.get(i).getScore();
            String text = texts.get(i);
            results.add(new VectorSearchResult(id, score, text));
        }

        return results;
    }

    /**
     * @param dim            向量维度
     * @param collectionName 集合名称
     *                       创建集合（如不存在）
     */
    public static void createCollectionIfNotExists(String collectionName, int dim) {
        HasCollectionParam hasCollectionParam = HasCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build();
        R<Boolean> hasCollection = client.hasCollection(hasCollectionParam);
        if (Boolean.TRUE.equals(hasCollection.getData())) {
            System.out.println("✅ 集合已存在: " + collectionName);
            return;
        }

        FieldType idField = FieldType.newBuilder()
                .withName("id")
                .withDescription("主键ID")
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(true)
                .build();

        FieldType vectorField = FieldType.newBuilder()
                .withName("embedding")
                .withDescription("向量字段")
                .withDataType(DataType.FloatVector)
                .withDimension(dim)
                .build();

        FieldType textField = FieldType.newBuilder()
                .withName("text")
                .withDescription("原始文本")
                .withDataType(DataType.VarChar)
                .withMaxLength(512)
                .build();

        CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .withDescription("自动创建集合")
                .withShardsNum(2)
                .addFieldType(idField)
                .addFieldType(vectorField)
                .addFieldType(textField)
                .build();

        client.createCollection(createCollectionParam);
        System.out.println("✅ 成功创建集合: " + collectionName);
    }

    /**
     * @param vectorField    向量字段名称
     * @param collectionName 集合名称
     *                       为向量字段创建基础索引
     */
    public static void createBasicIndex(String collectionName, String vectorField) {
        CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                .withCollectionName(collectionName)
                .withFieldName(vectorField)
                .withIndexName("basic_index")
                .withIndexType(IndexType.FLAT)
                .withMetricType(MetricType.L2)
                .withExtraParam("{}")
                .build();

        client.createIndex(indexParam);
        System.out.println("✅ 向量字段创建索引完成: " + vectorField);
    }

    /**
     * @param collectionName 集合名称
     *                       加载集合到内存
     */
    public static void loadCollection(String collectionName) {
        LoadCollectionParam loadParam = LoadCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build();
        client.loadCollection(loadParam);
        System.out.println("✅ 集合已加载进内存: " + collectionName);
    }

    /**
     * 关闭连接
     */
    public static void close() {
        client.close();
    }
}
