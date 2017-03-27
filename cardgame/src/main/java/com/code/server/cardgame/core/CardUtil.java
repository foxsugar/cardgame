package com.code.server.cardgame.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/21.
 */
public class CardUtil {
    protected static List<List<Integer>> typeCard = new ArrayList<>();//牌大小排列

    static {
        for(int i=9;i<=52;i+=4){
            List<Integer> l = new ArrayList<>();
            l.add(i);
            l.add(i+1);
            l.add(i+2);
            l.add(i+3);
            typeCard.add(l);
        }
        //把A和2,小王，大王放在最后
        List<Integer> cardA = new ArrayList<>();
        cardA.add(1);
        cardA.add(2);
        cardA.add(3);
        cardA.add(4);
        List<Integer> card2 = new ArrayList<>();
        card2.add(5);
        card2.add(6);
        List<Integer> cardxiao = new ArrayList<>();
        cardxiao.add(53);
        List<Integer> cardda = new ArrayList<>();
        cardda.add(54);

        typeCard.add(cardA);
        typeCard.add(card2);
        typeCard.add(cardxiao);
        typeCard.add(cardda);
    }


    public static Integer getTypeByCard (Integer card){
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


}
