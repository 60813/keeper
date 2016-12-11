//package com.keeper.test;
//import java.util.Properties;
//import java.util.concurrent.TimeUnit;
//
//import kafka.producer.KeyedMessage;
//import kafka.serializer.StringEncoder;
//
//import org.apache.kafka.clients.producer.Producer;
//import org.apache.kafka.clients.producer.ProducerConfig;
//import org.apache.kafka.clients.producer.ProducerRecord;
//  
//  
//  
//  
//public class kafkaProducer extends Thread{  
//  
//    private String topic;  
//      
//    public kafkaProducer(String topic){  
//        super();  
//        this.topic = topic;  
//    }  
//      
//      
//    @Override  
//    public void run() {  
//        Producer producer = createProducer();  
//        int i=0;  
//        while(true){  
//            producer.send(new ProducerRecord<String, String>(topic, i+"", "message"+i));  
//            try {  
//                TimeUnit.SECONDS.sleep(1);  
//            } catch (InterruptedException e) {  
//                e.printStackTrace();  
//            }  
//        }  
//    }  
//  
//    private Producer createProducer() {  
//        Properties properties = new Properties();  
//        properties.put("zookeeper.connect", "10.33.25.95:2181");//声明zk  
//        properties.put("serializer.class", StringEncoder.class.getName());  
//        properties.put("metadata.broker.list", "10.33.25.95:9092");// 声明kafka broker  
//        return new Producer<Integer, String>(new ProducerConfig);  
//     }  
//      
//      
//    public static void main(String[] args) {  
//        new kafkaProducer("test").start();// 使用kafka集群中创建好的主题 test   
//          
//    }  
//       
//}  