# 1、日志生成
使用python模拟生成日志文件
```
#coding=UTF-8

import random
import time

url_paths = [
    "class/112.html",
    "class/128.html",
    "class/145.html",
    "class/146.html",
    "class/131.html",
    "class/130.html",
    "learn/821",
    "course/list"
]

ip_slices = [132,168,175,10,23,179,187,224,73,29,90,169,48,89,120,67,138,168,220,221,98]

http_referers = [
    "http://www.baidu.com/s?wd={query}",
    "https://www.sogou.com/web?query={query}",
    "http://cn.bing.com/search?q={query}",
    "https://search.yahoo.com/search?p={query}" 
]

search_keyword = [
    "Spark实战",
    "Storm实战",
    "Flink实战",
    "Bean实战",
    "Spark Streaming实战",
    "Spark SQL实战"
]

status_codes = ["200","404","500"]

def sample_url():
    return random.sample(url_paths,1)[0]
    
def sample_ip():
    slice = random.sample(ip_slices,4)
    return ".".join([str(item) for item in slice])
    
def sample_referer():
    if random.uniform(0,1) > 0.2:
        return "-"
        
    refer_str = random.sample(http_referers,1)
    query_str = random.sample(search_keyword,1)
    return refer_str[0].format(query=query_str[0])
    
def sample_status_code():
    return random.sample(status_codes,1)[0]
    
def generate_log(count = 10):
    time_str = time.strftime("%Y-%m-%d %H:%M:%S",time.localtime())
    
    while count >= 1:
        query_log = "{ip}\t{local_time}\t\"GET /{url} HTTP/1.1\"\t{status_code}\t{referer}".format(local_time=time_str,url= sample_url(),ip=sample_ip(),referer=sample_referer(),status_code=sample_status_code())
        #print(query_log)
        print(query_log)
        count = count - 1
        
if __name__ == '__main__':
    generate_log(10)

```

# 2、使用Flume收集日志信息（使用双层架构）
## 1）exec-avro.conf 
```
agent1.sources = r1
agent1.channels = c1
agent1.sinks = k1

#define sources
agent1.sources.r1.type = exec
agent1.sources.r1.command = tail -F /home/qyl/logs/flume.log

#define channels
agent1.channels.c1.type = memory
agent1.channels.c1.capacity = 1000
agent1.channels.c1.transactionCapacity = 100

#define sink
agent1.sinks.k1.type = avro
agent1.sinks.k1.hostname = qyl02
agent1.sinks.k1.port = 44444

#bind sources and sink to channel
agent1.sources.r1.channels = c1
agent1.sinks.k1.channel = c1

```
## 2）avro-kafka.conf

```
agent2.sources = r2
agent2.channels = c2
agent2.sinks = k2

#define sources
agent2.sources.r2.type = avro
agent2.sources.r2.bind = qyl02
agent2.sources.r2.port = 44444

#define channels 
agent2.channels.c2.type = memory
agetn2.channels.c2.capacity = 1000
agent2.channels.c2.transactionCapacity = 100

#define sink
agent2.sinks.k2.type = org.apache.flume.sink.kafka.KafkaSink
agent2.sinks.k2.brokerList = qyl01:9092,qyl02:9092,qyl03:9092
agent2.sinks.k2.topic = flume-kafka-sparkStreaming-HBase
agent2.sinks.k2.batchSize = 4
agent2.sinks.k2.requiredAcks = 1

#bind sources and sink to channel
agent2.sources.r2.channels = c2
agent2.sinks.k2.channel =c2


```
## 3、启动Flume的Agent收集数据

```
bin/flume-ng agent \
--conf /home/qyl/apps/apache-flume-1.9.0-bin/conf/ \
--name agent2 \
--conf-file /home/qyl/apps/apache-flume-1.9.0-bin/agentconf/avro-kafka.conf \
-Dflume.root.logger=DEBUG,console


bin/flume-ng agent \
--conf /home/qyl/apps/apache-flume-1.9.0-bin/conf/ \
--name agent1 \
--conf-file /home/qyl/apps/apache-flume-1.9.0-bin/agentconf/exec-avro.conf \
-Dflume.root.logger=DEBUG,console

```

# 4、启动Kafka查看topic的数据
```
nohup kafka-server-start.sh \
/home/qyl/apps/kafka_2.11-1.1.0/config/server.properties \
1>/home/qyl/logs/kafka_std.log \
2>/home/qyl/logs/kafka_err.log &

```

###  启动消费者查看数据
```
  kafka-console-consumer.sh \
--bootstrap-server qyl01:9092,qyl02:9092,qyl03:9092 \
--from-beginning \
--topic flume-kafka-sparkStreaming-HBase
```




