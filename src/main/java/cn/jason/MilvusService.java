package cn.jason;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.SearchResults;
import io.milvus.param.*;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.SearchResultsWrapper;
import java.util.*;
/**
 * @describe: 封装了milvus的基本操作，简化使用
 * @Author JasonZhang
 * @Date 2025/4/14
**/
public class MilvusService {
    private final MilvusServiceClient client;
    /**
     * 实例化后自动连接Milvus服务，无需手动操作
     * */
    public MilvusService() {
        this.client = new MilvusServiceClient(
                ConnectParam.newBuilder().withHost("localhost").withPort(19530).build()
        );
    }

    public void insert(String collectionName, List<String> texts, List<List<Float>> vectors) {
        // 封装入库逻辑
        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(collectionName)
                .withFields(Arrays.asList(
                        new InsertParam.Field("embedding", vectors),   // 替换为你自己的字段名
                        new InsertParam.Field("text", texts)
                ))
                .build();

        R<MutationResult> insertResult = client.insert(insertParam);
        System.out.println("✅ 插入数据行数: " + insertResult.getData().getInsertCnt());
    }

    public List<VectorSearchResult> search(String collectionName, List<Float> queryVector) {
        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName(collectionName)
                .withMetricType(MetricType.L2)
                .withOutFields(Arrays.asList("id", "text"))
                .withTopK(3)
                .withVectors(Collections.singletonList(queryVector))
                .withVectorFieldName("embedding") // 替换为你的向量字段名
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

    public void close() {
        client.close();
    }
    public void createCollectionIfNotExists(String collectionName, int dim) {
        HasCollectionParam hasCollectionParam = HasCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build();
        R<Boolean> hasCollection = client.hasCollection(hasCollectionParam);
        if (Boolean.TRUE.equals(hasCollection.getData())) {
            System.out.println("✅ 集合已存在: " + collectionName);
            return;
        }

        // 定义字段（ID, 向量, 原始文本）
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
    public void createBasicIndex(String collectionName, String vectorField) {
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

    public void loadCollection(String collectionName) {
        LoadCollectionParam loadParam = LoadCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build();
        client.loadCollection(loadParam);
        System.out.println("✅ 集合已加载进内存: " + collectionName);
    }
}

