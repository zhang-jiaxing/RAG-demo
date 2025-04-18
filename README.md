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
``String qwen_api_key = System.getenv("DASHSCOPE_API_KEY");``  
``private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/embeddings";``  
``private final String API_KEY = qwen_api_key; // ← Replace with your actual API Key``
## Quick start
#### Start the Milvus service
recommend docker 
Please start the [Milvus service](https://milvus.io/docs/zh/prerequisite-docker.md) based on the environment of this machine.
#### run program
``
javac -cp ".:milvus-xxx-xxx-2.x.x.jar" xxx.java
``  
``
java -cp ".:milvus-xxx-xxx-2.x.x.jar" xxx
``
## Example output
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
