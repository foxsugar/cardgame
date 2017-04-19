package com.code.server.cardgame.response;

import com.code.server.cardgame.core.CardStruct;
import com.code.server.cardgame.core.PlayerCardInfo;
import com.code.server.cardgame.core.game.Game;
import com.code.server.cardgame.core.game.GameDouDiZhu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sunxianping on 2017/4/19.
 */
public class GameDoudizhuVo extends GameVo {

    protected List<Integer> tableCards = new ArrayList<>();//底牌
    protected Map<Long,PlayerCardInfoVo> playerCardInfos = new HashMap<>();
    protected long dizhu;//地主

    protected long canJiaoUser;//可以叫地主的人
    protected long canQiangUser;//可以抢地主的人
    protected long jiaoUser;//叫的人
    protected long qiangUser;//抢的人

    protected long playTurn;//该出牌的人
    protected CardStruct lastCardStruct;

    protected int step;//步骤
    protected int curMultiple;


    public static GameVo getGameVo(Game game, long uid){
        GameDoudizhuVo vo = new GameDoudizhuVo();
        if (game instanceof GameDouDiZhu) {
            GameDouDiZhu douDiZhu = (GameDouDiZhu) game;

            //设置地主
            vo.dizhu = douDiZhu.getDizhu();
            vo.step = douDiZhu.getStep();
            vo.canJiaoUser = douDiZhu.getCanJiaoUser();
            vo.canQiangUser = douDiZhu.getCanQiangUser();
            vo.jiaoUser = douDiZhu.getJiaoUser();
            vo.qiangUser = douDiZhu.getQiangUser();
            vo.lastCardStruct = douDiZhu.getLastCardStruct();
            //该出牌的玩家
            vo.playTurn = douDiZhu.getPlayTurn();
            vo.curMultiple = douDiZhu.getMultiple();
            if(uid == douDiZhu.getDizhu()){//玩家是地主
                vo.tableCards.addAll(douDiZhu.getTableCards());

            }

            //玩家牌信息
            for (PlayerCardInfo playerCardInfo : douDiZhu.getPlayerCardInfos().values()) {
                vo.playerCardInfos.put(playerCardInfo.userId, new PlayerCardInfoVo(playerCardInfo, uid));
            }

        }
        return vo;

    }
}
