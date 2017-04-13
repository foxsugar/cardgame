package com.code.server.cardgame.core.room;

import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.core.game.Game;
import com.code.server.cardgame.core.game.GameDouDiZhu;
import com.code.server.cardgame.core.game.GameTianDaKeng;
import com.code.server.cardgame.encoding.Notice;
import com.code.server.cardgame.response.*;
import com.code.server.cardgame.timer.GameTimer;
import com.code.server.cardgame.timer.TimerNode;
import com.code.server.db.model.User;
import com.google.gson.Gson;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * Created by sunxianping on 2017/3/13.
 */
public class RoomTanDaKeng extends Room{


    private static final Logger logger = Logger.getLogger(RoomTanDaKeng.class);

    protected Game getGameInstance(){
        return new GameTianDaKeng();
    }




    public static int createRoom(Player player,int gameNumber,double roomType,int personNumber){
        if(GameManager.getInstance().userRoom.containsKey(player.getUserId())){
            return ErrorCode.CANNOT_CREATE_ROOM_ROLE_HAS_IN_ROOM;
        }
        int needMoney = getNeedMoney(gameNumber);
        if (player.getUser().getMoney() < needMoney) {
            return ErrorCode.CANNOT_CREATE_ROOM_MONEY;
        }
        RoomTanDaKeng room = new RoomTanDaKeng();
        room.personNumber = personNumber;

        room.roomId = getRoomIdStr(genRoomId());
        room.createUser = player.getUserId();
        room.init(gameNumber,roomType);
        //房间加入列表
        room.roomAddUser(player);
        GameManager.getInstance().rooms.put(room.roomId, room);

        player.sendMsg(new ResponseVo("roomService","createRoom",new RoomTianDaKengVo(room,player)));

        return 0;
    }




    public static int getNeedMoney(int gameNumber) {
        if (gameNumber == 32) {
            return 1;
        } else {
            return 1;
        }

    }

    public void init(int gameNumber, double roomType) {
        this.gameNumber = gameNumber;
        this.roomType = roomType;
        this.isInGame = false;
    }


}
