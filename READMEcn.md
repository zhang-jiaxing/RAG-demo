## RAG-demo
Java+Milvus,构建一个rag应用
## 环境版本
milvus 2.5.5+jdk 17+aliyun-bailian text-embedding-v3 qwen-turbo
## 简介
本项目采用中国刑法向量数据库中导出的十组模拟数据作为模拟的向量数据。
本示例演示了如何使用Milvus构建法律条款的矢量检索系统。通过文本的矢量化表示，它可以快速检索类似的法律条款。
系统会根据用户输入的问题返回最相关的刑法条文。
## 应用配置
#### milvus 连接
#### 路径 cn/jason/MilvusService.java
``MilvusServiceClient(ConnectParam.newBuilder().withHost("host").withPort(port).build());``
#### 机械模型配置
#### 路径 cn/jason/QwenClient.java（EmbeddingClient.java）
``参数 API_KEY,MODEL_NAME,API_URL``
## 快速开始
#### 启动milvus服务
推荐docker
请根据本机的环境去启动[milvus服务](https://milvus.io/docs/zh/prerequisite-docker.md)
#### 运行程序（支持idea）
``javac -cp ".:milvus-xxx-xxx-2.x.x.jar" xxx.java``  
``java -cp ".:milvus-xxx-xxx-2.x.x.jar" xxx`` 
## 示例输出
```
模型回答：
根据中国《刑法》的相关规定，你的行为可能涉及以下罪名：

1. **贪污罪**（《中华人民共和国刑法》第382条）：如果这些公家财物是通过职务上的便利非法占有，那么你可能构成贪污罪。

2. **职务侵占罪**（《中华人民共和国刑法》第270条）：如果你利用职务上的便利，将单位财物非法占为己有，数额较大的，可能构成职务侵占罪。

3. **非法经营罪**（《中华人民共和国刑法》第225条）：如果转卖行为涉及扰乱市场秩序，且情节严重，也可能构成非法经营罪。

4. **盗窃罪**（《中华人民共和国刑法》第264条）：如果是以秘密窃取的方式获取公家财物，则可能构成盗窃罪。

具体适用哪一条，需要根据案件的具体情况来判断，包括但不限于涉案金额、行为方式、是否利用职务便利等。建议尽快咨询专业律师以获得准确的法律意见。
RAG回答
您提到的行为可能同时触犯了《中华人民共和国刑法》中的贪污罪和诈骗罪，具体如下：

1. **贪污罪**：根据《中华人民共和国刑法》第382条的规定，如果您是国家工作人员，并且利用职务上的便利非法占有公共财物，那么您的行为构成贪污罪。

2. **诈骗罪**：如果您的行为不符合贪污罪的构成要件（例如您不是国家工作人员），但您通过欺骗手段非法占有公共财物并转卖他人，那么根据《中华人民共和国刑法》第266条的规定，您的行为可能构成诈骗罪。

### 建议
无论触犯的是哪一条刑法，非法占有公共财物的行为都是严重违法行为，可能会受到法律的严厉制裁。建议您尽快停止此类行为，并积极配合相关部门调查，争取从轻处理的可能性。同时，加强法律知识的学习，避免再次触犯法律。
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
