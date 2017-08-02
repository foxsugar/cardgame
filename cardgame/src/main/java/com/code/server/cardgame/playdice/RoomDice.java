package com.code.server.cardgame.playdice;

import com.code.server.cardgame.core.Game;
import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.core.Room;
import com.code.server.cardgame.response.ErrorCode;
import com.code.server.cardgame.response.ResponseVo;
import com.code.server.cardgame.timer.GameTimer;
import com.code.server.cardgame.timer.ITimeHandler;
import com.code.server.cardgame.timer.TimerNode;

import java.util.ArrayList;
import java.util.List;

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

    public static final long FOUR_HOUR = 1000L * 60 * 60 * 4;

    //玩家状态:

    //private int isSelf;//代开房,0代开房，1自己用

    private int curCricleNumber=1;
    private Long curBanker;

    protected Game getGameInstance(){
        return new GameDice();
    }

    public void init(int cricle, int personNumber, int isSelf) {
        this.cricle = cricle;
        this.personNumber = personNumber;
        this.isSelf = isSelf;
        this.isInGame = false;
        this.createNeedMoney = cricle==1?3:5;
        this.lastOperateTime = System.currentTimeMillis();
    }


    public static int createRoom(Player player, int cricle, int personNumber, int isSelf){
        if(GameManager.getInstance().userRoom.containsKey(player.getUserId())){
            return ErrorCode.CANNOT_CREATE_ROOM_ROLE_HAS_IN_ROOM;
        }
        int needMoney = getNeedMoney(cricle);

        int notSelfUserMoney = 0;
        List<Room>  notSelfRoomList = GameManager.getInstance().userRoomList.get(player.getUserId());
        if(notSelfRoomList!=null){
            for (Room room:notSelfRoomList) {
                if(!room.isInGame()){
                    if(room.getCricle()==1){
                        notSelfUserMoney+=3;
                    }else{
                        notSelfUserMoney+=5;
                    }
                }
            }
        }
        if (player.getUser().getMoney() < needMoney + notSelfUserMoney) {
            return ErrorCode.CANNOT_CREATE_ROOM_MONEY;
        }

        RoomDice room = new RoomDice();
        room.personNumber = personNumber;
        room.roomId = getRoomIdStr(genRoomId());
        room.createUser = player.getUserId();
        room.setCreateType(Room.ROOM_CREATE_TYPE_DICE);
        room.init(cricle,personNumber,isSelf);

        //房间加入列表
        if(isSelf==1){
            room.roomAddUser(player);
        }else{
            long start = System.currentTimeMillis();
            TimerNode node = new TimerNode(start, FOUR_HOUR, false, new ITimeHandler() {
                @Override
                public void fire() {
                    try {
                        if(!room.isInGame()){
                            GameManager.getInstance().rooms.remove(room.roomId);
                            //删除带开房的房间
                            List<Room> roomList = new ArrayList<>();
                            roomList = GameManager.getInstance().userRoomList.get(player.getUserId());
                            roomList.remove(room);
                            GameManager.getInstance().userRoomList.put(player.getUserId(),roomList);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            room.timerNode = node;
            GameTimer.getInstance().addTimerNode(node);
            //记录到带开房的列表
            List<Room> roomList = GameManager.getInstance().userRoomList.get(player.getUserId());
            if(roomList==null){
                roomList = new ArrayList<>();
                roomList.add(room);
                GameManager.getInstance().userRoomList.put(player.getUserId(),roomList);
                System.out.println(GameManager.getInstance().userRoomList.get(player.getUserId()).size());
            }else{
                roomList.add(room);
                GameManager.getInstance().userRoomList.put(player.getUserId(),roomList);
            }
        }
        GameManager.getInstance().rooms.put(room.roomId, room);

        player.sendMsg(new ResponseVo("roomService","createRoomDice",new RoomDiceVo(room,player)));

        return 0;
    }

    public static int searchRoomDice(Player player, Long createId){
        GameManager.getInstance().userRoomList.get(createId);
        player.sendMsg(new ResponseVo("roomService","searchRoomDice",GameManager.getInstance().userRoomList.get(createId)));
        return 0;
    }

    public static int getNeedMoney(int cricle) {
        return cricle==1?3:5;
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

    public Long getCurBanker() {
        return curBanker;
    }

    public void setCurBanker(Long curBanker) {
        this.curBanker = curBanker;
    }

}
