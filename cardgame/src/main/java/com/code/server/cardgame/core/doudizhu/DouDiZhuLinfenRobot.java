package com.code.server.cardgame.core.doudizhu;

import com.code.server.cardgame.core.Game;
import com.code.server.cardgame.core.GoldRoomPool;
import com.code.server.cardgame.core.IGameConstant;
import com.code.server.cardgame.core.Room;

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
            if(now > game.lastOperateTime + SECOND * 30){
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
    @Override
    public void jiaoDizhu(GameDouDiZhu game) {

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
