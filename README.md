# RAG-demo
Java+Milvus,Build an RAG application
## Environment version
milvus 2.5.5+jdk 17+aliyun-bailian text-embedding-v3
## brief introduction
This project uses ten sets of simulated data derived from the vector database of the Chinese Criminal Law as the vector data for the simulation.
This example demonstrates how to build a vector retrieval system for legal provisions using Milvus. Through the vectorization representation of text, it enables fast retrieval of similar legal provisions. The system will return the most relevant criminal law provisions based on the user's input question.
## Application configuration
##### milvus connect
``MilvusServiceClient client = new MilvusServiceClient(
                ConnectParam.newBuilder().withHost("localhost").withPort(19530).build());``
##### Mechanical model configuration
``String qwen_api_key = System.getenv("DASHSCOPE_API_KEY");
    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/embeddings";
    private final String API_KEY = qwen_api_key; // ← Replace with your actual API Key``
## Quick start
#### Start the Milvus service
recommend docker 
Please start the [Milvus service](https://milvus.io/docs/zh/prerequisite-docker.md) based on the environment of this machine.
#### run program
``
javac -cp ".:milvus-xxx-xxx-2.x.x.jar" xxx.java
java -cp ".:milvus-xxx-xxx-2.x.x.jar" xxx
``
## Example output
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
