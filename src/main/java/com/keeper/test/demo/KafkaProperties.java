package com.keeper.test.demo;
/**
 *@author huangdou
 *@at 2016年12月9日上午10:05:07
 *@version 0.0.1
 */

public class KafkaProperties {
    public static final String TOPIC = "a0";
    public static final String KAFKA_SERVER_URL = "10.33.25.95";
    public static final int KAFKA_SERVER_PORT = 9093;
    public static final int KAFKA_PRODUCER_BUFFER_SIZE = 64 * 1024;
    public static final int CONNECTION_TIMEOUT = 100000;
    public static final String TOPIC2 = "a1";
    public static final String TOPIC3 = "a2";
    public static final String CLIENT_ID = "SimpleConsumerDemoClient";

    private KafkaProperties() {}
}
