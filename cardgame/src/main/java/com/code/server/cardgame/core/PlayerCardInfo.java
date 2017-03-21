package com.code.server.cardgame.core;

import com.code.server.cardgame.core.room.RoomDouDiZhu;

import java.util.*;

/**
 * Created by sunxianping on 2017/3/14.
 */
public class PlayerCardInfo {
    public long userId;
    public List<Integer> cards = new ArrayList<>();//手上的牌
    protected List<Integer> disCards = new ArrayList<>();//丢弃的牌
    protected List<List<Integer>> typeCard = new ArrayList<>();//牌大小排列


    public void init(){
        initCards();
        //乱序
        Collections.shuffle(cards);

        //初始化牌次序大小
        for(int i=9;i<=52;i+=4){
            List<Integer> l = new ArrayList<>();
            l.add(i);
            l.add(i+1);
            l.add(i+2);
            l.add(i+3);
            typeCard.add(l);
        }
        //把A和2,小王，大王放在最后
        List<Integer> CardA = new ArrayList<>();
        CardA.add(1);
        CardA.add(2);
        CardA.add(3);
        CardA.add(4);
        List<Integer> Card2 = new ArrayList<>();
        Card2.add(5);
        Card2.add(6);
        List<Integer> Cardxiao = new ArrayList<>();
        Cardxiao.add(53);
        List<Integer> Cardda = new ArrayList<>();
        Cardda.add(54);

        typeCard.add(CardA);
        typeCard.add(Card2);
        typeCard.add(Cardxiao);
        typeCard.add(Cardda);

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
        for(int i=0;i<=typeCard.size();i++){
            if(card==typeCard.get(i).get(0)){
                return i;
            }else if(card==typeCard.get(i).get(1)){
                return i;
            }else if(card==typeCard.get(i).get(2)){
                return i;
            }else if(card==typeCard.get(i).get(3)){
                return i;
            }
        }
        return 0;
    }

    public Integer getListByIsType(List<Integer> cards) {
        int len = cards.size();
        if (len <= 4) {
            if (cards.size() > 0 && cards.get(0) == cards.get(len - 1)) {
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
            if (len == 2 && getTypeByCard(cards.get(0)) == 12 && getTypeByCard(cards.get(1)) == 12) {
                return CardStruct.type_炸;
            }
            if (len == 2 && getTypeByCard(cards.get(0)) == 13 && getTypeByCard(cards.get(1)) == 14) {
                return CardStruct.type_火箭;
            }
            if (len == 4 && getTypeByCard(cards.get(0)) == getTypeByCard(cards.get(len - 2)) && getTypeByCard(cards.get(1)) == getTypeByCard(cards.get(len - 1))) {
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
            if (len == 6 && getTypeByCard(cards.get(0)) == getTypeByCard(cards.get(len - 3))
                    && getTypeByCard(cards.get(1)) == getTypeByCard(cards.get(len - 3))
                    && getTypeByCard(cards.get(2)) == getTypeByCard(cards.get(len - 3))) {
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





