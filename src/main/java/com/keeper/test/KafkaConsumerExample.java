package com.keeper.test;

import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

/**
 *@author huangdou
 *@at 2016年12月6日上午9:37:04
 *@version 0.0.1
 */
public class KafkaConsumerExample {  
    public static void main(String[] args) {  
        Properties props = new Properties();  
        
//        props.put("bootstrap.servers", "10.33.25.95:9092");
        props.put("zookeeper.connect", "10.6.134.16:2181");//声明zk  
        props.put("group.id", "test");  
        props.put("enable.auto.commit", "true");  
        props.put("auto.commit.interval.ms", "1000");  
        props.put("session.timeout.ms", "30000");  
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");  
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");  
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);  
        
        consumer.subscribe(Arrays.asList("topic1"));  
        while (true) {  
            ConsumerRecords<String, String> records = consumer.poll(100);  
            for (ConsumerRecord<String, String> record : records)  
                System.out.printf("offset = %d, key = %s, value = %s\n", record.offset(), record.key(), record.value());  
        }  
    }  
}  
