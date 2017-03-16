package com.code.server.cardgame.response;

import com.code.server.cardgame.core.PlayerCardInfo;
import com.code.server.cardgame.core.game.Game;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by sunxianping on 2017/3/15.
 */
public class GameVo {


    protected List<Integer> cards = new ArrayList<>();//牌
    protected List<Integer> tableCards = new ArrayList<>();//底牌
    protected Map<Long,PlayerCardInfo> playerCardInfos = new HashMap<>();
    protected List<Long> users = new ArrayList<>();
    protected long dizhu;//地主
    protected Set<Long> chooseJiaoSet = new HashSet<>();
    protected Set<Long> chooseQiangSet = new HashSet<>();
    protected Set<Long> bujiaoSet = new HashSet<>();
    private long canJiaoUser;//可以叫地主的人
    private long canQiangUser;//可以抢地主的人
    private long jiaoUser;//叫的人
    private long qiangUser;//抢的人
    private int jiaoIndex;//第几个叫的

    private long playTurn;//该出牌的人

    private int step;//步骤

    public GameVo(){}

    public static GameVo getGameVo(Game game){
        GameVo vo = new GameVo();
        try {
            BeanUtils.copyProperties(vo,game);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
       return vo;

    }
}
