package com.code.server.cardgame.core;

import java.util.*;

/**
 * Created by sunxianping on 2017/3/14.
 */
public class PlayerCardInfoTianDaKeng {
    public long userId;
    public List<Integer> cards = new ArrayList<>();//手上的牌



    public void init(){
        initCards();
        //乱序
        Collections.shuffle(cards);

    }
    protected void initCards(){
        for (int i = 37; i < 53; i+=4) {
            cards.add(i);
        }
        cards.add(1);
        cards.add(2);
        cards.add(3);
        cards.add(4);
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
        //判断牌型是否合法
        if(getListByIsType(currentCardStruct.cards) == 0){
            return false;
        }
        if(0!=lasttype){
             Integer currenttype =  currentCardStruct.type;//获取当前出牌类型
             if(currenttype==lasttype){
                List<Integer> lastList = lastcardStruct.getByTypeList(lasttype);//获取上次出牌的牌型
                List<Integer> list = currentCardStruct.getByTypeList(currenttype);//获取当前出牌类型

                 if(list.size()>lastList.size()){     //3333 > 22
                    results = true;
                 }else if(getTypeByCard(list.get(0))>getTypeByCard(lastList.get(0))){
                    results = true;
                }
            }else if(currenttype==CardStruct.type_火箭){ // 出牌是火箭
                 results = true;
             }else if(lasttype<CardStruct.type_炸 &&  currenttype == CardStruct.type_炸){ //出牌是炸弹，并且上一次出牌的类型不是火箭也不是炸弹
                 results = true;
             }
        }else{
            results = true;
        }
        return results;
    }

    public Integer getTypeByCard (Integer card){
        for(int i=0;i<=CardUtil.typeCard.size();i++){
            if(card.intValue()==CardUtil.typeCard.get(i).get(0).intValue()){
                return i;
            }else if(card.intValue()==CardUtil.typeCard.get(i).get(1).intValue()){
                return i;
            }else if(card.intValue()==CardUtil.typeCard.get(i).get(2).intValue()){
                return i;
            }else if(card.intValue()==CardUtil.typeCard.get(i).get(3).intValue()){
                return i;
            }
        }
        return 0;
    }

    public Integer getListByIsType(List<Integer> cards) {
        int len = cards.size();
        if (len <= 4) {
            if (cards.size() > 0 && cards.get(0).intValue() == cards.get(len - 1).intValue()) {
                switch (len) {
                    case 1:
                        return CardStruct.type_单;
                    case 2:
                        return CardStruct.type_对;
                    case 3:
                        return CardStruct.type_三;
                    case 4:
                        return CardStruct.type_炸;
                }
            }
            if (len == 2 && getTypeByCard(cards.get(0)).intValue() == 12
                    && getTypeByCard(cards.get(1)).intValue() == 12) {
                return CardStruct.type_炸;
            }
            if (len == 2 && getTypeByCard(cards.get(0)).intValue() == 13
                    && getTypeByCard(cards.get(1)).intValue() == 14) {
                return CardStruct.type_火箭;
            }
            if (len == 4 && getTypeByCard(cards.get(0)).intValue() == getTypeByCard(cards.get(len - 2)).intValue()
                    && getTypeByCard(cards.get(1)).intValue() == getTypeByCard(cards.get(len - 1)).intValue()) {
                return CardStruct.type_三带单;
            } else {
                return 0;
            }
        }

        if (len >= 5) {
            if (getTypeByCard(cards.get(0)) != 13 && getTypeByCard(cards.get(len - 1)) - getTypeByCard(cards.get(0)) == len - 1) {
                return CardStruct.type_顺;
            }
            if (len % 2 == 0 && (len / 2 == 3 || len / 2 > 3) && getTypeByCard(cards.get(len - 1)) - getTypeByCard(cards.get(0)) == len / 2 - 1) {
                return CardStruct.type_连对;
            }
            if (len == 6 && getTypeByCard(cards.get(0)).intValue() == getTypeByCard(cards.get(len - 3)).intValue()
                    && getTypeByCard(cards.get(1)).intValue() == getTypeByCard(cards.get(len - 3)).intValue()
                    && getTypeByCard(cards.get(2)).intValue() == getTypeByCard(cards.get(len - 3)).intValue()) {
                return CardStruct.type_四带二;
            }
            if (len % 3 == 0 && (len / 3 == 2 || len / 3 > 2) && getTypeByCard(cards.get(len - 1)) - getTypeByCard(cards.get(0)) == len / 3 - 1) {
                return CardStruct.type_飞机;
            }
            List<Integer> cardList = new ArrayList<>();
            for(Integer card :cards){
                cardList.add(getTypeByCard(card));
            }
            if (getfeijichibang(cards)) {
                return CardStruct.type_飞机带翅膀;
            } else {
                return 0;
            }

        }else{
            return 0;
        }

    }

    public boolean getfeijichibang(List<Integer> cards){
        boolean b = true;
        Map<Integer,Integer> map = new HashMap<>();
        List<Integer> threelist = new ArrayList<>();
        List<Integer> twolist = new ArrayList<>();
        List<Integer> onelist = new ArrayList<>();
        for (Integer i:cards) {
            if(map.containsKey(i)){
                map.put(i,map.get(i)+1);
            }else{
                map.put(i,1);
            }
        }
        for (Integer i:map.keySet()) {
            if(map.get(i)==3){
                threelist.add(i);
            }else if(map.get(i)==2){
                twolist.add(i);
            }else{
                onelist.add(i);
            }
        }
        Collections.sort(threelist);

        if (onelist.size()!=0 && twolist.size()!=0){
            b=false;
        }
        for(int i = 0 ;i<threelist.size()-1;i++){
            if(threelist.get(i+1) - threelist.get(i) != 1){
                b =false;
            }
        }
        return b;
    }



}





