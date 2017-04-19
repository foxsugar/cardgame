package com.code.server.cardgame.core;

import java.util.*;

/**
 * Created by sunxianping on 2017/3/14.
 */
public class PlayerCardInfoTianDaKeng {
    public long userId;
    public List<Integer> myselfCards = new ArrayList<>();//手上的牌(暗)
    public List<Integer> everyknowCards = new ArrayList<>();//手上的牌(明)
    public List<Integer> allCards = new ArrayList<>();//手上的牌


    //getter and setter==============================================================================
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public List<Integer> getMyselfCards() {
        return myselfCards;
    }

    public void setMyselfCards(List<Integer> myselfCards) {
        this.myselfCards = myselfCards;
    }

    public List<Integer> getEveryknowCards() {
        return everyknowCards;
    }

    public void setEveryknowCards(List<Integer> everyknowCards) {
        this.everyknowCards = everyknowCards;
    }

    public List<Integer> getAllCards() {
        return allCards;
    }

    public void setAllCards(List<Integer> allCards) {
        this.allCards = allCards;
    }

}





