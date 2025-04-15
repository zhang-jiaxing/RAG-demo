## RAG-demo
Java+Milvus,构建一个rag应用
## 环境版本
milvus 2.5.5+jdk 17+aliyun-bailian text-embedding-v3
## 简介
本项目采用中国刑法向量数据库中导出的十组模拟数据作为模拟的向量数据。
本示例演示了如何使用Milvus构建法律条款的矢量检索系统。通过文本的矢量化表示，它可以快速检索类似的法律条款。
系统会根据用户输入的问题返回最相关的刑法条文。
## 应用配置
#### milvus 连接
``MilvusServiceClient client = new MilvusServiceClient( ConnectParam.newBuilder().withHost("localhost").withPort(19530).build());``
#### 机械模型配置 （EmbeddingClient.java,QwenClient.java）
``String qwen_api_key = System.getenv("DASHSCOPE_API_KEY");``  
``private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/embeddings";``  
``private final String API_KEY = qwen_api_key; // ← Replace with your actual API Key``
## 快速开始
#### 启动milvus服务
推荐docker
请根据本机的环境去启动[milvus服务](https://milvus.io/docs/zh/prerequisite-docker.md)
#### 运行程序（支持idea）
``javac -cp ".:milvus-xxx-xxx-2.x.x.jar" xxx.java``  
``java -cp ".:milvus-xxx-xxx-2.x.x.jar" xxx`` 
## 示例输出
```
✅ 集合已加载进内存: law_articles
2025-04-15 10:47:04 io.milvus.client.AbstractMilvusGrpcClient logInfo 
INFO: SearchParam{collectionName='law_articles', partitionNames='[]', metricType=L2, target vectors count=1, vectorFieldName='embedding', topK=3, nq=1, expr='', params='{"nprobe": 10}', consistencyLevel='null', ignoreGrowing='false'}
🎯 匹配结果:
ID: 457333793497293200
相似度得分: 0.46925
原文: 中华人民共和国刑法（第234条）: 故意伤害他人身体的，处三年以下有期徒刑、拘役或者管制。致人重伤的，处三年以上十年以下有期徒刑；致人死亡或造成严重残疾的，处十年以上有期徒刑、无期徒刑或者死刑。
----------
🎯 匹配结果:
ID: 457333793497293212
相似度得分: 0.46925
原文: 中华人民共和国刑法（第234条）: 故意伤害他人身体的，处三年以下有期徒刑、拘役或者管制。致人重伤的，处三年以上十年以下有期徒刑；致人死亡或造成严重残疾的，处十年以上有期徒刑、无期徒刑或者死刑。
----------
🎯 匹配结果:
ID: 457333793497293195
相似度得分: 0.6582297
原文: 中华人民共和国刑法（第232条）: 故意杀人的，处死刑、无期徒刑或者十年以上有期徒刑；情节较轻的，处三年以上十年以下有期徒刑。
----------
🤖 AI 回答：
您故意伤害他人身体且未导致死亡，触犯了《中华人民共和国刑法》第234条关于故意伤害罪的规定。
```
**************************************
以上针对RagApp的描述和应用就结束了，如果你没有使用过milvus或其他向量数据库，可以留意MyApp.java该应用只针对对milvus的创建插入删除检索等对数据库的应用。
## 核心功能说明
#### 数据结构
* id	Int64	主键（自动生成）
* text	VarChar(512)	法律条文文本
* embedding	FloatVector	1024维文本向量
#### 关键配置参数
```
// 集合配置
private static final String COLLECTION_NAME = "test_demo_collection";
private static final String VECTOR_FIELD = "embedding";
private static final int VECTOR_DIM = 1024;  // 必须与 embedding 维度一致

// 检索参数
.withTopK(3)                // 返回前3个结果
.withMetricType(MetricType.L2)  // 使用L2距离
.withParams("{\"nprobe\": 10}") // 搜索精度参数
```
#### MyApp示例输出
```
✅ 集合创建成功
✅ 插入数据行数: 10
✅ 索引创建结果: SUCCESS
🔍 检索结果数量：3
📌 ID = 5, 距离 = 0.3542, 文本 = 中华人民共和国刑法（第234条）...
📌 ID = 2, 距离 = 0.4218, 文本 = 中华人民共和国刑法（第133条）...
📌 ID = 9, 距离 = 0.4783, 文本 = 中华人民共和国刑法（第299条）...
```

#### 向量检索流程
```mermaid
graph TD
    A[连接 Milvus] --> B[清理旧集合]
    B --> C[创建新集合]
    C --> D[生成文本向量]
    D --> E[插入数据]
    E --> F[创建索引]
    F --> G[加载集合]
    G --> H[执行检索]
