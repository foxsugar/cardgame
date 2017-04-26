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

    public static final int NEEDMOENY = 1;

    private static final Logger logger = Logger.getLogger(RoomTanDaKeng.class);

    private boolean isLastDraw = false;//是否平局

    private int drawForLeaveChip = 0;//平局留下筹码


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
        room.multiple = (int)roomType;
        room.roomId = getRoomIdStr(genRoomId());
        room.createUser = player.getUserId();
        room.init(gameNumber,roomType);

        if(room.getMultiple()!=25 && room.getMultiple()!=50 && room.getMultiple()!=100 && room.getMultiple()!=200){
            return ErrorCodeTDK.CREATE_ROOM_MULTIPLE;
        }

        //房间加入列表
        room.roomAddUser(player);
        GameManager.getInstance().rooms.put(room.roomId, room);

        player.sendMsg(new ResponseVo("roomService","createRoomTDK",new RoomTianDaKengVo(room,player)));

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

    public boolean isLastDraw() {
        return isLastDraw;
    }

    public void setLastDraw(boolean lastDraw) {
        isLastDraw = lastDraw;
    }

    public int getDrawForLeaveChip() {
        return drawForLeaveChip;
    }

    public void setDrawForLeaveChip(int drawForLeaveChip) {
        this.drawForLeaveChip = drawForLeaveChip;
    }


    public void spendMoney() {
        User user = userMap.get(this.createUser);
        if (user != null) {
            user.setMoney(user.getMoney() - NEEDMOENY);
            GameManager.getInstance().getSaveUser2DB().add(user);
        }
    }
}
