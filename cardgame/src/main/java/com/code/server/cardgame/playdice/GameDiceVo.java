package com.code.server.cardgame.playdice;

import com.code.server.cardgame.core.Game;
import com.code.server.cardgame.response.GameVo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目名称：${project_name}
 * 类名称：${type_name}
 * 类描述：
 * 创建人：Clark
 * 创建时间：${date} ${time}
 * 修改人：Clark
 * 修改时间：${date} ${time}
 * 修改备注：
 *
 * @version 1.0
 */
public class GameDiceVo extends GameVo{

    protected Map<Long,Integer> gameUserStatus = new HashMap<>();

    protected Map<Long,Double> gameUserScore = new HashMap<>();

    protected Map<Long,Double> gameResultScore = new HashMap<>();//结局分数

    protected Map<Long,PlayerCardInfoDiceVo> playerCardInfos = new HashMap<>();

    protected Map<Long,List<Integer>> allDiceNumber = new HashMap<>();//所有玩家点数

    private long currentTurn;

    public static GameVo getGameDiceVo(Game game, long uid){
        GameDiceVo vo = new GameDiceVo();
        if(game!=null){
            GameDice gameDice = (GameDice) game;
            vo.gameUserStatus = gameDice.getGameUserStatus();
            vo.gameUserScore = gameDice.getGameUserScore();
            vo.gameResultScore = gameDice.getGameResultScore();
            vo.allDiceNumber = gameDice.getAllDiceNumber();
            //玩家牌信息
            for (PlayerCardInfoDice playerCardInfo : gameDice.getPlayerCardInfos().values()) {
                vo.playerCardInfos.put(playerCardInfo.userId, new PlayerCardInfoDiceVo(playerCardInfo, uid));
            }
        }
        return vo;

    }
}
