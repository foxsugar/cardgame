package com.code.server.cardgame.core;

import java.util.*;

import static com.code.server.cardgame.core.CardUtil.getCardType;

/**
 * Created by sunxianping on 2017/3/14.
 */
public class PlayerCardInfo {
    public long userId;
    public List<Integer> cards = new ArrayList<>();//手上的牌
    protected List<Integer> disCards = new ArrayList<>();//丢弃的牌
    private boolean isQiang;
    private double score;
    private int playCount;





    public boolean isCanPlay(CardStruct cardStruct){

        return false;
    }


    public boolean isCanCard(CardStruct cardStruct){

        return false;
    }

    //检测出牌是否合法
    public boolean checkPlayCard(CardStruct lastcardStruct ,CardStruct currentCardStruct , int lasttype,List<Integer> cards){
        if (lastcardStruct == null || lastcardStruct.getUserId()==0) {
            return true;
        }
        if (lastcardStruct.getUserId() == userId) {
            return true;
        }

        List<Integer> copyCards = new ArrayList<>();
        copyCards.addAll(cards);

        if(currentCardStruct.getType() == CardStruct.type_三 || currentCardStruct.getType() == CardStruct.type_飞机){
            copyCards.removeAll(currentCardStruct.getCards());
            if(copyCards.size()<=0){
                return true;
            }else{
                return false;
            }
        }

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
                 }else if(CardUtil.getTypeByCard(list.get(0))>CardUtil.getTypeByCard(lastList.get(0))){
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

    private boolean isSameType(List<Integer> cards) {
        Set<Integer> types = new HashSet<>();
        for (int card : cards) {
            int cardType = getCardType(card);
            types.add(cardType);
        }
        return types.size() == 1;
    }
    public Integer getListByIsType(List<Integer> cards) {
        int len = cards.size();
        if (len <= 4) {
            if (cards.size() > 0 &&isSameType(cards)) {
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
            if (len == 2 && CardUtil.getTypeByCard(cards.get(0)) == 12
                    && CardUtil.getTypeByCard(cards.get(1)) == 12) {
                return CardStruct.type_炸;
            }
            if (len == 2 && CardUtil.getTypeByCard(cards.get(0)) == 0
                    && CardUtil.getTypeByCard(cards.get(1)) == 0) {
                return CardStruct.type_炸;
            }
            if (len == 2 && CardUtil.getTypeByCard(cards.get(0)) == 13
                    && CardUtil.getTypeByCard(cards.get(1)) == 14) {
                return CardStruct.type_火箭;
            }
            if (len == 4 && CardUtil.getTypeByCard(cards.get(0)).intValue() == CardUtil.getTypeByCard(cards.get(len - 2)).intValue()
                    && CardUtil.getTypeByCard(cards.get(0)).intValue() == CardUtil.getTypeByCard(cards.get(len - 3)).intValue()) {
                return CardStruct.type_三带单;
            } else {
                return 0;
            }
        }

        if (len >= 5) {
            if (CardUtil.getTypeByCard(cards.get(0)) != 13 && CardUtil.getTypeByCard(cards.get(len - 1)) - CardUtil.getTypeByCard(cards.get(0)) == len - 1) {
                return CardStruct.type_顺;
            }
            if (len % 2 == 0 && (len / 2 == 3 || len / 2 > 3) && CardUtil.getTypeByCard(cards.get(len - 1)) - CardUtil.getTypeByCard(cards.get(0)) == len / 2 - 1) {
                return CardStruct.type_连对;
            }
            if (len == 6 && CardUtil.getTypeByCard(cards.get(0)).intValue() == CardUtil.getTypeByCard(cards.get(len - 3)).intValue()
                    && CardUtil.getTypeByCard(cards.get(1)).intValue() == CardUtil.getTypeByCard(cards.get(len - 3)).intValue()
                    && CardUtil.getTypeByCard(cards.get(2)).intValue() == CardUtil.getTypeByCard(cards.get(len - 3)).intValue()) {
                return CardStruct.type_四带二;
            }
            if (len % 3 == 0 && (len / 3 == 2 || len / 3 > 2) && CardUtil.getTypeByCard(cards.get(len - 1)) - CardUtil.getTypeByCard(cards.get(0)) == len / 3 - 1) {
                return CardStruct.type_飞机;
            }
            List<Integer> cardList = new ArrayList<>();
            for(Integer card :cards){
                cardList.add(CardUtil.getTypeByCard(card));
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


    public long getUserId() {
        return userId;
    }

    public PlayerCardInfo setUserId(long userId) {
        this.userId = userId;
        return this;
    }

    public List<Integer> getCards() {
        return cards;
    }

    public PlayerCardInfo setCards(List<Integer> cards) {
        this.cards = cards;
        return this;
    }

    public List<Integer> getDisCards() {
        return disCards;
    }

    public PlayerCardInfo setDisCards(List<Integer> disCards) {
        this.disCards = disCards;
        return this;
    }

    public boolean isQiang() {
        return isQiang;
    }

    public PlayerCardInfo setQiang(boolean qiang) {
        isQiang = qiang;
        return this;
    }

    public double getScore() {
        return score;
    }

    public PlayerCardInfo setScore(double score) {
        this.score = score;
        return this;
    }

    public int getPlayCount() {
        return playCount;
    }

    public PlayerCardInfo setPlayCount(int playCount) {
        this.playCount = playCount;
        return this;
    }
}





