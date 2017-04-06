package com.code.server.cardgame.service;

import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.core.room.Room;
import com.code.server.cardgame.encoding.Notice;
import com.code.server.cardgame.response.ErrorCode;
import org.springframework.stereotype.Service;

/**
 * Created by sunxianping on 2017/3/28.
 */
@Service
public class GameChatService {


    public int sendMessageToOne(Player player, long acceptUserId,String messageType, String message) {
        // messageType,1表示普通打字，2表示表情，3表示语音
        long sendUserId = player.getUserId();
        Room room = GameManager.getInstance().getRoomByUser(player.getUserId());
        if (room == null) {
            return ErrorCode.CAN_NOT_NO_ROOM;
        }

        Notice sendNotice = new Notice();
        Notice acceptNotice = new Notice();
        sendNotice.setMessage("send message success!");
        sendNotice.setMessageType(messageType);
        acceptNotice.setMessage(message);
        acceptNotice.setMessageType(messageType);
        acceptNotice.setAcceptUserId("" + acceptUserId);
        acceptNotice.setSendUserId("" + sendUserId);

//        JSONObject noticeResult = new JSONObject();
//
//        noticeResult.put("service", "gameService");
//        noticeResult.put("method", "acceptMessage");
//        noticeResult.put("params", acceptNotice.toJSONObject());
//        noticeResult.put("code", "0");
//
//        result.put("service", "gameService");
//        result.put("method", "sendMessage");
//        result.put("params", sendNotice.toJSONObject());
//        result.put("code", "0");
        Player.sendMsg2Player("chatService", "acceptMessage", acceptNotice, room.getUsers());
        player.sendMsg("chatService", "sendMessage", sendNotice);


        return 0;
    }



    public int sendMessage(Player player, String messageType,
                                  String message) {
        // messageType,1表示普通打字，2表示表情，3表示语音
        long userId = player.getUserId();
        Room room = GameManager.getInstance().getRoomByUser(userId);
        if (room == null) {
            return ErrorCode.CAN_NOT_NO_ROOM;
        }




        Notice sendNotice = new Notice();
        Notice acceptNotice = new Notice();
        sendNotice.setMessage("send message success!");
        sendNotice.setMessageType(messageType);
        sendNotice.setSendUserId(""+userId);
        acceptNotice.setMessage(message);
        acceptNotice.setMessageType(messageType);
        acceptNotice.setSendUserId(""+userId);



        Player.sendMsg2Player("chatService","acceptMessage",acceptNotice,room.getUsers());

        player.sendMsg("chatService","sendMessage",sendNotice);





        return 0;
    }
}
