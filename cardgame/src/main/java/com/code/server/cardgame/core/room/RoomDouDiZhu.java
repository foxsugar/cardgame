package com.code.server.cardgame.core.room;

import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.game.Game;
import com.code.server.cardgame.core.game.GameDouDiZhu;
import com.code.server.db.model.User;
import org.apache.log4j.Logger;

/**
 * Created by sunxianping on 2017/3/13.
 */
public class RoomDouDiZhu extends Room{



    public static final int STATUS_JOIN = 0;
    public static final int STATUS_READY = 1;
    public static final int STATUS_IN_GAME = 2;
    public static final int STATUS_DISSOLUTION = 3;
    public static final int STATUS_AGREE_DISSOLUTION = 4;

    public static final long FIVE_MIN = 1000L * 60 * 5;
    public static final int PERSONNUM = 3;

    protected boolean isCanDissloution = false;


    protected Game getGameInstance(){
        return new GameDouDiZhu();
    }


    public void spendMoney() {
        User user = userMap.get(this.createUser);
        if (user != null) {
            user.setMoney(user.getMoney() - createNeedMoney);
            GameManager.getInstance().getSaveUser2DB().add(user);
        }
    }

    private void sendRpcRebat(){

    }

}
