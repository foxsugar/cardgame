package com.code.server.cardgame.handler;



import com.code.server.cardgame.core.MsgDispatch;
import com.code.server.cardgame.Message.MessageHolder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by sun on 2015/8/21.
 *
 */
public class GameProcessor implements Runnable{
    @Autowired
    private MsgDispatch messageHandler;

    private GameProcessor(){}

    public static GameProcessor instance;

    public LinkedBlockingQueue<MessageHolder> messageQueue = new LinkedBlockingQueue<>();
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


           //timer
           MessageHolder messHolder = null;
           try {
               messHolder = messageQueue.poll(10, TimeUnit.MILLISECONDS);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
           if(messHolder != null&&messHolder.message !=null){
               handler.handleMessage(messHolder);
//               MessageHandler handler1 = ApplicationContext.getBean(MessageHandler.class);

           }
//           System.out.println("------------");
       }
    }

}
