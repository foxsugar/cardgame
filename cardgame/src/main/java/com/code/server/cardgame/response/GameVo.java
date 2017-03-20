package com.code.server.cardgame.response;

import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.core.PlayerCardInfo;
import com.code.server.cardgame.core.game.Game;
import com.code.server.cardgame.core.game.GameDouDiZhu;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by sunxianping on 2017/3/15.
 */
public class GameVo {


    protected List<Integer> cards = new ArrayList<>();//牌
    protected List<Integer> tableCards = new ArrayList<>();//底牌
    protected Map<Long,PlayerCardInfoVo> playerCardInfos = new HashMap<>();
    protected List<Long> users = new ArrayList<>();
    protected long dizhu;//地主

    private long canJiaoUser;//可以叫地主的人
    private long canQiangUser;//可以抢地主的人
    private long jiaoUser;//叫的人
    private long qiangUser;//抢的人

    private long playTurn;//该出牌的人

    private int step;//步骤

    public GameVo(){}

    public static GameVo getGameVo(Game game, Player player){
        GameVo vo = new GameVo();
        if (game instanceof GameDouDiZhu) {
            GameDouDiZhu douDiZhu = (GameDouDiZhu) game;

            //设置地主
            vo.dizhu = douDiZhu.getDizhu();
            vo.step = douDiZhu.getStep();
            vo.canJiaoUser = douDiZhu.getCanJiaoUser();
            vo.canQiangUser = douDiZhu.getCanQiangUser();
            vo.jiaoUser = douDiZhu.getJiaoUser();
            vo.qiangUser = douDiZhu.getQiangUser();
            //该出牌的玩家
            vo.playTurn = douDiZhu.getPlayTurn();
            if(player.getUserId() == douDiZhu.getDizhu()){//玩家是地主
                vo.tableCards.addAll(douDiZhu.getTableCards());

            }

            //玩家牌信息
            for (PlayerCardInfo playerCardInfo : douDiZhu.getPlayerCardInfos().values()) {
                vo.playerCardInfos.put(playerCardInfo.userId, new PlayerCardInfoVo(playerCardInfo, player));
            }

        }
        return vo;

    }
}
