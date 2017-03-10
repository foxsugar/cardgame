package com.code.server.cardgame.handler;



import com.code.server.cardgame.Message.MessageHandler;
import com.code.server.cardgame.Message.MessageHolder;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by sun on 2015/8/21.
 *
 */
public class GameProcessor implements Runnable{

    private GameProcessor(){}

    public static GameProcessor instance;

    public LinkedBlockingQueue<MessageHolder> messageQueue = new LinkedBlockingQueue<>();
    public MessageHandler handler = new MessageHandler();

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
               System.out.println("-111111111111111");
               handler.handleMessage(messHolder);
           }
       }
    }

}
