package com.code.server.cardgame.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/21.
 */
public class CardUtilOfTangDaKeng {
    protected static List<List<Integer>> typeCard = new ArrayList<>();//牌大小排列

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
    }


}
