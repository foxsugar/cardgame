package com.code.server.cardgame.core.tiandakeng;

import com.code.server.cardgame.core.Game;
import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.core.Room;
import com.code.server.cardgame.response.*;
import com.code.server.db.model.Record;
import com.code.server.db.model.User;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * Created by sunxianping on 2017/3/13.
 */
public class RoomTanDaKeng extends Room {

    public static final int NEEDMOENY = 1;

    private static final Logger logger = Logger.getLogger(RoomTanDaKeng.class);

    private boolean isLastDraw = false;//是否平局

    private int drawForLeaveChip = 0;//平局留下筹码

    private Long dealFirstOfRoom;//第一个发牌的人

    protected Game getGameInstance(){
        return new GameTianDaKeng();
    }


    public static int createRoom(Player player, int gameNumber, double roomType, int personNumber, int hasNine){
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
        room.hasNine = hasNine;
        room.init(gameNumber,roomType,hasNine);

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

    public void init(int gameNumber, double roomType,int hasNine) {
        this.gameNumber = gameNumber;
        this.roomType = roomType;
        this.multiple = (int)roomType;
        this.hasNine = hasNine;
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

    public Long getDealFirstOfRoom() {
        return dealFirstOfRoom;
    }

    public void setDealFirstOfRoom(Long dealFirstOfRoom) {
        this.dealFirstOfRoom = dealFirstOfRoom;
    }

    public void spendMoney() {
        User user = userMap.get(this.createUser);
        if (user != null) {
            user.setMoney(user.getMoney() - NEEDMOENY);
            GameManager.getInstance().getSaveUser2DB().add(user);
        }
    }

    /**
     * 解散房间
     */
    protected void dissolutionRoom(){

        GameManager.getInstance().removeRoom(this);


        // 结果类
        ArrayList<UserOfResult> userOfResultList = new ArrayList<>();

        long time = System.currentTimeMillis();

        Record.RoomRecord roomRecord = new Record.RoomRecord();
        roomRecord.setTime(System.currentTimeMillis());
        roomRecord.setType(this.getCreateType());

        for(User user : this.userMap.values()){
            UserOfResult resultObj = new UserOfResult();
            try {
                resultObj.setUsername(URLDecoder.decode(user.getUsername(), "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            resultObj.setImage(user.getImage());
            resultObj.setScores(""+this.userScores.get(user.getUserId())*this.multiple/100);
            resultObj.setUserId(user.getUserId());
            resultObj.setTime(time);

            Record.UserRecord userRecord = new Record.UserRecord();
            userRecord.setName(user.getUsername());
            userRecord.setScore(this.userScores.get(user.getUserId())*this.multiple/100);
            userRecord.setUserId(user.getUserId());
            userRecord.setRoomId(this.roomId);

            roomRecord.addRecord(userRecord);

            userOfResultList.add(resultObj);
            //删除映射关系
            GameManager.getInstance().getUserRoom().remove(user.getUserId());
        }

        this.getUserMap().forEach((k,v)->v.getRecord().addRoomRecord(roomRecord));
        GameManager.getInstance().getSaveUser2DB().addAll(this.getUserMap().values());


        boolean isChange = scoreIsChange();
        if (this.isInGame && this.curGameNumber == 1 && !isChange) {
//            drawBack();
        }



        this.isInGame = false;
        // 存储返回
        GameOfResult gameOfResult = new GameOfResult();

        gameOfResult.setUserList(userOfResultList);
        gameOfResult.setEndTime(new Date().toLocaleString());

        JSONObject noticeEndResult = new JSONObject();
        noticeEndResult.put("service", "gameService");
        noticeEndResult.put("method", "askNoticeDissolutionResult");
        noticeEndResult.put("params", gameOfResult);
        noticeEndResult.put("code", "0");
        Player.sendMsg2Player(noticeEndResult, this.users);

    }

    protected void roomRemoveUser(Player player) {
        User user = player.getUser();
        long userId = user.getUserId();
        this.users.remove(userId);
        this.userStatus.remove(userId);
        GameManager.getInstance().getUserRoom().remove(userId);
    }
}
