package com.code.server.cardgame.core.game;

import com.code.server.cardgame.core.PlayerCardInfo;

import java.util.*;

/**
 * Created by sunxianping on 2017/3/13.
 */
public class GameDouDiZhu extends Game{
    private static final int initCardNum = 16;

    protected long userId;
    protected List<Integer> cards = new ArrayList<>();//牌
    protected List<Integer> disCards = new ArrayList<>();//丢弃的牌
    protected List<Integer> tableCards = new ArrayList<>();//底牌
    protected List<PlayerCardInfo> playerCardInfos = new ArrayList<>();
    protected List<Long> jiaoList = new ArrayList<>();
    protected long dizhu;
    protected List<Long> users = new ArrayList<>();



    public void init(List<Long> users,long dizhuUser){
        //初始化玩家
        for(Long uid : users){
            PlayerCardInfo playerCardInfo = new PlayerCardInfo();
            playerCardInfo.userId = uid;
            playerCardInfos.add(playerCardInfo);
        }


        shuffle();
        deal();
        chooseDizhu(dizhuUser);

    }

    /**
     * 洗牌
     */
    protected void shuffle(){
        for(int i=1;i<=54;i++){
            cards.add(i);
        }
        //去掉两张2
        cards.remove((Integer)7);
        cards.remove((Integer)8);
        Collections.shuffle(cards);
    }

    /**
     * 发牌
     */
    protected void deal(){
        for(PlayerCardInfo playerCardInfo : playerCardInfos){
            playerCardInfo.cards.addAll(cards.subList(0, 15));
        }
    }

    protected void chooseDizhu(long lastDizhu) {
        //随机叫地主
        if(lastDizhu == 0){

        }
    }

    protected void jiaoDizhu(long lastDizhu){

    }

    public long nextTurnId(int curId) {
        int index = users.indexOf(curId);

        int nextId = index + 1;
        if (nextId >= users.size()) {
            nextId = 0;
        }
        return users.get(nextId);
    }




    public void startGame(List<Long> users,long dizhuUser){

    }
}
