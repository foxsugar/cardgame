package com.code.server.cardgame.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/3/21.
 */
public class CardUtilOfTangDaKeng {
    protected static List<List<Integer>> typeCard = new ArrayList<>();//牌大小排列
    protected static Map<Integer,Integer> cardForScore = new HashMap<>();//牌的点数

    static {
        for (int i = 37; i < 53; i+=4) {
            List<Integer> list = new ArrayList<Integer>();
            list.add(i);
            list.add(i+1);
            list.add(i+2);
            list.add(i+3);
            typeCard.add(list);
        }
        //4张A
        List<Integer> cardA = new ArrayList<>();
        cardA.add(1);
        cardA.add(2);
        cardA.add(3);
        cardA.add(4);
        typeCard.add(cardA);

        int temp = 10;
        for (int i = 37; i < 53; i+=4) {
            cardForScore.put(i,temp);
            cardForScore.put(i+1,temp);
            cardForScore.put(i+2,temp);
            cardForScore.put(i+3,temp);
            temp++;
        }
        cardForScore.put(1,15);
        cardForScore.put(2,15);
        cardForScore.put(3,15);
        cardForScore.put(4,15);
    }

    public static List<List<Integer>> getTypeCard() {
        return typeCard;
    }

    public static void setTypeCard(List<List<Integer>> typeCard) {
        CardUtilOfTangDaKeng.typeCard = typeCard;
    }

    public static Map<Integer, Integer> getCardForScore() {
        return cardForScore;
    }

    public static void setCardForScore(Map<Integer, Integer> cardForScore) {
        CardUtilOfTangDaKeng.cardForScore = cardForScore;
    }
}
