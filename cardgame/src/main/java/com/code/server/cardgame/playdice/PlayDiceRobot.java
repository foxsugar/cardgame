package com.code.server.cardgame.playdice;

import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.IGameConstant;
import com.code.server.cardgame.core.Room;
import com.code.server.cardgame.handler.GameProcessor;
import com.code.server.cardgame.handler.MessageHolder;
import com.code.server.cardgame.response.ResponseVo;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sunxianping on 2017/5/16.
 */
public class PlayDiceRobot implements IPlayDiceRobot,IGameConstant {

    @Override
    public void execute() {
        GameManager.getInstance().rooms.values().forEach(this::doExecute);
    }

    private void doExecute(Room room){
        if (room == null) {
            return;
        }
        if(room != null && room.getGame() == null){//准备
            if (room instanceof RoomDice) {
                long now = System.currentTimeMillis();
                if(now > room.lastOperateTime + SECOND * 15){
                    for (Long l : room.getUserStatus().keySet()) {
                        if(0==room.getUserStatus().get(l)){
                            getReady(room,l+"");
                        }
                    }
                }
            }
        }
        if (room.getGame() instanceof GameDice) {
            GameDice game = (GameDice) room.getGame();
            long now = System.currentTimeMillis();
            //执行
            if(now > game.lastOperateTime + SECOND * 15){

            }
        }
    }

    @Override
    public void bet(GameDice game) {

    }

    @Override
    public void rock(GameDice game) {

    }

    @Override
    public void kill(GameDice game) {

    }

    @Override
    public void killAll(GameDice game) {

    }

    @Override
    public void getReady(Room room,String userId) {
        Map<String, String> params = new HashMap<>();
        params.put("roomId", room.getRoomId());
        params.put("userId", userId);
        ResponseVo vo = new ResponseVo("roomService","getReady",params);
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.msgType = MessageHolder.MSG_TYPE_INNER;
        messageHolder.userId = Long.parseLong(userId);
        JSONObject jsonObject = JSONObject.fromObject(vo);
        messageHolder.message = jsonObject;
        GameProcessor.getInstance().messageQueue.add(messageHolder);
    }
}
