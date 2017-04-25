package com.code.server.cardgame.response;

import com.code.server.cardgame.core.PlayerCardInfoTianDaKeng;
import com.code.server.cardgame.core.game.Game;
import com.code.server.cardgame.core.game.GameTianDaKeng;
import com.code.server.cardgame.core.room.RoomTanDaKeng;

import java.util.*;

/**
 * Created by sunxianping on 2017/3/15.
 */
public class GameTianDaKengVo extends GameVo{


    protected List<Integer> cards = new ArrayList<>();//牌

    protected List<Integer> tableCards = new ArrayList<>();//剩余牌
    protected Map<Long,PlayerCardInfoTianDaKengVo> playerCardInfos = new HashMap<>();
    protected List<Long> users = new ArrayList<>();
    protected Random rand = new Random();

    protected Map<Long,Double> allChip = new HashMap<>();//总下注数
    protected Map<Long,Double> curChip = new HashMap<>();//当前下注数


    protected long currentTurn;//当前操作人
    protected int chip;//下注
    protected int trunNumber;//第几张牌了


    protected List<Long> aliveUser = new ArrayList<>();//存活的人
    protected List<Long> curUser = new ArrayList<>();//本轮的人
    protected List<Long> canRaiseUser = new ArrayList<>();//可以反踢的人



    public static GameTianDaKengVo getGameTianDaKengVo(Game game, long uid){
        GameTianDaKengVo vo = new GameTianDaKengVo();
        if(game!=null){
            GameTianDaKeng tianDaKeng = (GameTianDaKeng) game;

            vo.cards = tianDaKeng.getCards();
            vo.tableCards = tianDaKeng.getTableCards();
            vo.users = tianDaKeng.getUsers();
            vo.allChip = tianDaKeng.getAllChip();
            vo.curChip = tianDaKeng.getCurChip();
            vo.currentTurn = tianDaKeng.getCurrentTurn();
            vo.chip = tianDaKeng.getChip();
            vo.trunNumber = tianDaKeng.getTrunNumber();
            vo.aliveUser = tianDaKeng.getAliveUser();
            vo.curUser = tianDaKeng.getCurUser();
            vo.canRaiseUser = tianDaKeng.getCanRaiseUser();

            //玩家牌信息
            for (PlayerCardInfoTianDaKeng playerCardInfo : tianDaKeng.getPlayerCardInfos().values()) {
                vo.playerCardInfos.put(playerCardInfo.userId, new PlayerCardInfoTianDaKengVo(playerCardInfo, uid));
            }
        }
        return vo;

    }
}
