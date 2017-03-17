package com.code.server.cardgame.handler;



import com.code.server.cardgame.core.MsgDispatch;
import com.code.server.cardgame.Message.MessageHolder;
import com.code.server.cardgame.timer.GameTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by sun on 2015/8/21.
 *
 */
public class GameProcessor implements Runnable{


    private static final Logger logger = LoggerFactory.getLogger(GameProcessor.class);

    private GameProcessor(){}

    public static GameProcessor instance;

    public LinkedBlockingQueue<MessageHolder> messageQueue = new LinkedBlockingQueue<>(1000);

    public MsgDispatch handler = new MsgDispatch();

    public static GameProcessor getInstance(){
        if(instance == null){
            instance = new GameProcessor();
        }
        return instance;
    }



    @Override
    public void run() {
       while(true){
           try {
               MessageHolder messHolder = messageQueue.poll(10, TimeUnit.MILLISECONDS);
               if(messHolder != null&&messHolder.message !=null){
                   handler.handleMessage(messHolder);
               }
               //定时任务
               GameTimer.getInstance().handle();
           } catch (Exception e) {
               logger.error("handle message error ",e);
           }
       }
    }

}
