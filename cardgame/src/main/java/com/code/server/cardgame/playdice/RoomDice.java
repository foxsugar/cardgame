package com.code.server.cardgame.playdice;

import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.core.Room;
import com.code.server.cardgame.response.ErrorCode;
import com.code.server.cardgame.response.ResponseVo;

/**
 * 项目名称：${project_name}
 * 类名称：${type_name}
 * 类描述：
 * 创建人：Clark
 * 创建时间：${date} ${time}
 * 修改人：Clark
 * 修改时间：${date} ${time}
 * 修改备注：
 *
 * @version 1.0
 */
public class RoomDice extends Room {

    private int cricle;
    private int isSelf;//代开房,0代开房，1自己用

    private int curCricleNumber;
    private int curBanker;

    public void init(int cricle, int personNumber, int isSelf) {
        this.cricle = cricle;
        this.personNumber = personNumber;
        this.isSelf = isSelf;
    }


    public static int createRoom(Player player, int cricle, int personNumber, int isSelf){
        if(GameManager.getInstance().userRoom.containsKey(player.getUserId())){
            return ErrorCode.CANNOT_CREATE_ROOM_ROLE_HAS_IN_ROOM;
        }
        int needMoney = getNeedMoney(cricle,personNumber);
        if (player.getUser().getMoney() < needMoney) {
            return ErrorCode.CANNOT_CREATE_ROOM_MONEY;
        }

        RoomDice room = new RoomDice();
        room.personNumber = personNumber;
        room.roomId = getRoomIdStr(genRoomId());
        room.createUser = player.getUserId();
        room.init(cricle,personNumber,isSelf);

        //房间加入列表
        if(isSelf==1){
            room.roomAddUser(player);
        }
        GameManager.getInstance().rooms.put(room.roomId, room);

        player.sendMsg(new ResponseVo("roomService","createRoomDice",new RoomDiceVo(room,player)));

        return 0;
    }


    public static int getNeedMoney(int cricle,int gameNumber) {
        return cricle * gameNumber;
    }

    public int getCricle() {
        return cricle;
    }

    public void setCricle(int cricle) {
        this.cricle = cricle;
    }

    public int getIsSelf() {
        return isSelf;
    }

    public void setIsSelf(int isSelf) {
        this.isSelf = isSelf;
    }

    public int getCurCricleNumber() {
        return curCricleNumber;
    }

    public void setCurCricleNumber(int curCricleNumber) {
        this.curCricleNumber = curCricleNumber;
    }

    public int getCurBanker() {
        return curBanker;
    }

    public void setCurBanker(int curBanker) {
        this.curBanker = curBanker;
    }
}
