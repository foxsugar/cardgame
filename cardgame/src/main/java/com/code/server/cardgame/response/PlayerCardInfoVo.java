package com.code.server.cardgame.response;

import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.core.PlayerCardInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunxianping on 2017/3/20.
 */
public class PlayerCardInfoVo {
    public long userId;
    public List<Integer> cards = new ArrayList<>();//手上的牌
    public int cardNum;

    public PlayerCardInfoVo() {
    }

    public PlayerCardInfoVo(PlayerCardInfo playerCardInfo, long uid) {
        this.userId = playerCardInfo.userId;
        if (playerCardInfo.userId == uid) {
            this.cards.addAll(playerCardInfo.cards);
        } else {
            this.cardNum = playerCardInfo.cards.size();
        }
    }

    public PlayerCardInfoVo(PlayerCardInfo playerCardInfo) {
        this.userId = playerCardInfo.userId;
        this.cards.addAll(playerCardInfo.cards);
    }
}
