package com.code.server.cardgame.core.doudizhu;

import com.code.server.cardgame.core.*;
import com.code.server.cardgame.handler.GameProcessor;
import com.code.server.cardgame.handler.MessageHolder;
import com.code.server.cardgame.response.ResponseVo;
import com.google.gson.Gson;
import net.sf.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sunxianping on 2017/5/16.
 */
public class DouDiZhuLinfenRobot implements IDouDiZhuRobot,IGameConstant {


    @Override
    public void execute() {
        GoldRoomPool.getInstance().getFullRoom().values().forEach(list->list.forEach(this::doExecute));
    }

    private void doExecute(Room room){
        if (room == null || room.getGame() == null) {
            return;
        }
        if (room.getGame() instanceof GameDouDiZhu) {
            GameDouDiZhu game = (GameDouDiZhu) room.getGame();
            long now = System.currentTimeMillis();
            //执行
            if(now > game.lastOperateTime + SECOND * 5){
                switch (game.step) {
                    case STEP_JIAO_DIZHU:
                        jiaoDizhu(game);
                        break;
                    case STEP_QIANG_DIZHU:
                        qiangDizhu(game);
                        break;
                    case STEP_PLAY:
                        play(game);
                        break;
                }
            }
        }

    }
    //{"service":"gameService","method":"jiaoDizhu","params":{"isJiao":true}}
    @Override
    public void jiaoDizhu(GameDouDiZhu game) {
        Map<String, Boolean> jiao = new HashMap<>();
        jiao.put("isJiao", false);
        ResponseVo vo = new ResponseVo("gameService","jiaoDizhu",jiao);
//        String json = gson.toJson(vo);
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.msgType = MessageHolder.MSG_TYPE_INNER;
        messageHolder.userId = game.canJiaoUser;
        JSONObject jsonObject = JSONObject.fromObject(vo);
        messageHolder.message = jsonObject;
        GameProcessor.getInstance().messageQueue.add(messageHolder);


    }

    @Override
    public void qiangDizhu(GameDouDiZhu game) {

    }

    @Override
    public void play(GameDouDiZhu game) {

    }

    @Override
    public void pass(GameDouDiZhu game) {

    }

}
