package com.code.server.cardgame.core;

import com.code.server.cardgame.core.room.RoomDouDiZhu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by sunxianping on 2017/3/14.
 */
public class PlayerCardInfo {
    public long userId;
    public List<Integer> cards = new ArrayList<>();//手上的牌
    protected List<Integer> disCards = new ArrayList<>();//丢弃的牌



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
    public boolean checkPlayCard(CardStruct lastcardStruct ,CardStruct currentCardStruct , int lasttype){
        boolean results = false;
        if(0!=lasttype){
            Integer currenttype =  currentCardStruct.type;//获取当前出牌类型
             if(currenttype==lasttype){
                List<Integer> lastList = lastcardStruct.getByTypeList(lasttype);
                List<Integer> list = currentCardStruct.getByTypeList(currenttype);

                if(list.get(0)>lastList.get(0)){
                    results = true;
                }
            }else if(currenttype==12){
                 results = true;
             }else if(lasttype<11 &&  currenttype == 11){
                 results = true;
             }
        }else{
            results = true;
        }
        return results;
    }
}
