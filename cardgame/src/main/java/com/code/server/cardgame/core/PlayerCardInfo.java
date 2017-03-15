package com.code.server.cardgame.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by sunxianping on 2017/3/14.
 */
public class PlayerCardInfo {
    protected List<Integer> cards = new ArrayList<>();//手上的牌
    protected List<Integer> disCards = new ArrayList<>();//丢弃的牌

    protected CardStruct lastcardStruct = new CardStruct();//上一个人出的牌
    protected int lasttype = 0;//上一个人出牌的类型

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
        boolean results = false;
        if(0!=lasttype){
            Integer type =  lastcardStruct.type;//获取出牌类型

            if(type>lasttype){
                results = true;
            }else if(type==lasttype){
                List<Integer> lastList = lastcardStruct.getByTypeList(type);
                List<Integer> list = cardStruct.getByTypeList(cardStruct.type);

                if(list.get(0)>lastList.get(0)){
                    results = true;
                }
            }
            lasttype = cardStruct.type;   //保存上一次出牌的类型
        }else{
            results = true;
        }

        return results;
    }
}
