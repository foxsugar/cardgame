package com.code.server.cardgame.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by sunxianping on 2017/3/14.
 */
public class PlayerCardInfo {
    public long userId;
    public List<Integer> cards = new ArrayList<>();//手上的牌
    protected List<Integer> disCards = new ArrayList<>();//丢弃的牌

    protected CardStruct onCard = new CardStruct();//上一个人出的牌

    public void init(){
        initCards();
        //乱序
        Collections.shuffle(cards);

    }
    protected void initCards(){
        for(int i=1;i<=54;i++){
            cards.add(i);
        }
        //去掉两张2
        cards.remove((Integer)7);
        cards.remove((Integer)8);
    }

    /**
     * 发牌
     */
    protected void deal(){

    }


    public boolean isCanPlay(CardStruct cardStruct){

        return false;
    }


    public boolean isCanCard(CardStruct cardStruct){

        return false;
    }

    //检测出牌是否合法
    public boolean checkPlayCard(CardStruct cardStruct){
        List<Integer> cardList = onCard.cards;

        return false;
    }
}
