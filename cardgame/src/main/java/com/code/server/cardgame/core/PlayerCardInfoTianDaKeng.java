package com.code.server.cardgame.core;

import java.util.*;

/**
 * Created by sunxianping on 2017/3/14.
 */
public class PlayerCardInfoTianDaKeng {
    public long userId;
    public List<Integer> myselfCards = new ArrayList<>();//手上的牌(暗)
    public List<Integer> everyknowCards = new ArrayList<>();//手上的牌(明)



    public boolean isCanPlay(CardStruct cardStruct){

        return false;
    }


    public boolean isCanCard(CardStruct cardStruct){

        return false;
    }



}





