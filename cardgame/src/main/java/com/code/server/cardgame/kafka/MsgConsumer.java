package com.code.server.cardgame.kafka;

import com.code.server.db.model.Constant;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 消息消费者
 * @author  2017/3/24 14:36
 */
@Component
public class MsgConsumer {

    int count = 0;

    @KafkaListener(id = "bar", topicPartitions =
            { @TopicPartition(topic = "test", partitions = {  "${serverConfig.serverId}"  })})
    public void processMessage(String content) {

        count++;
        System.out.println("开始接受消息---game");
        System.out.println(content);
//        try {
//            Thread.sleep(10);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        System.out.println("处理完毕");
        System.out.println(count);

    }


    @KafkaListener(id = "qux", topicPattern = "myTopic1")
    public void listen(@Payload String foo,
                       @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) Integer key,
                       @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {



    }




}