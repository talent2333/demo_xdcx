import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author xitianyu
 * @description
 * @date 2021/3/23
 */
public class TimestampConsumer {

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put("bootstrap.servers", "10.133.19.105:9092,10.133.19.106:9092,10.133.19.107:9092");
        props.put("group.id", "test-xitianyu");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        String topic = "dwd-passenger_fence_event_detail_h3";

        try {
            List<PartitionInfo> partitionInfos = consumer.partitionsFor(topic);
            ArrayList<TopicPartition> topicPartitions = new ArrayList<>();

            HashMap<TopicPartition, Long> topicPartitionLongHashMap = new HashMap<>();

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            //30分钟前的timestamp
            LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(60);
            long timestamp_30min = localDateTime.toEpochSecond(ZoneOffset.of("+8"));

            for (PartitionInfo partitionInfo : partitionInfos) {
                topicPartitions.add(new TopicPartition(partitionInfo.topic(), partitionInfo.partition()));
                topicPartitionLongHashMap.put(new TopicPartition(partitionInfo.topic(), partitionInfo.partition())
                        , timestamp_30min);
            }
            consumer.assign(topicPartitions);
            //取出30min前的偏移量
            Map<TopicPartition, OffsetAndTimestamp> map
                    = consumer.offsetsForTimes(topicPartitionLongHashMap);
            OffsetAndTimestamp offsetAndTimestamp = null;

            System.out.println("开始设置各分区初始偏移量");
            for (Map.Entry<TopicPartition, OffsetAndTimestamp> entry : map.entrySet()) {
                TopicPartition topicPartition = entry.getKey();
                offsetAndTimestamp = entry.getValue();
                if (offsetAndTimestamp != null) {
                    int partition = topicPartition.partition();
                    long timestamp = offsetAndTimestamp.timestamp();
                    long offset = offsetAndTimestamp.offset();
                    System.out.println("partition = " + partition + ", timestamp = " + timestamp + ", offset = " + offset);
                    //set partition's offset
                    consumer.seek(topicPartition,offset);
                }
            }
            System.out.println("各分区定位完成");
            while (true){
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("partition = " + record.partition() + ", offset = " + record.offset() +
                            ", data = " + record.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }
    }

}
