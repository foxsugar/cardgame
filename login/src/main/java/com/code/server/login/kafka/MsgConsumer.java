package com.code.server.login.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 消息消费者
 * @author  2017/3/24 14:36
 */
@Component
public class MsgConsumer {
    @KafkaListener(topics = {"test","my-replicated-topic2"})
    public void processMessage(String content) {
        System.out.println("开始接受消息");
        System.out.println(content);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("处理完毕");
        System.out.println("");

    }




}