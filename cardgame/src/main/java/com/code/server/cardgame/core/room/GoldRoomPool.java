package com.code.server.cardgame.core.room;

import com.code.server.cardgame.core.GameManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.code.server.cardgame.core.room.Room.ROOM_CREATE_TYPE_GOLD;
import static com.code.server.cardgame.core.room.RoomDouDiZhu.PERSONNUM;
import static com.code.server.cardgame.core.room.RoomDouDiZhu.genRoomId;
import com.code.server.cardgame.core.Player;

/**
 * 项目名称：${project_name}
 * 类名称：${type_name}
 * 类描述：存储匹配房间
 * 创建人：Clark
 * 创建时间：${date} ${time}
 * 修改人：Clark
 * 修改时间：${date} ${time}
 * 修改备注：
 *
 * @version 1.0
 */
public class GoldRoomPool {

    private GoldRoomPool() {
    }

    private static GoldRoomPool instance;

    public static GoldRoomPool getInstance() {
        if (instance == null) {
            instance = new GoldRoomPool();
        }
        return instance;
    }

    public static final int GAME_PERSON_NUMBER = 3;//游戏人数
    Map<Double, RoomDouDiZhuGold> roomLock = new HashMap<>();

    Map<Double, List<Room>> fullRoom = new HashMap<>();
    Map<Double,List<Room>> notFullRoom = new HashMap<>();


    public Map<Double, RoomDouDiZhuGold> getRoomLock() {
        return roomLock;
    }

    public void setRoomLock(Map<Double, RoomDouDiZhuGold> roomLock) {
        this.roomLock = roomLock;
    }

    public int addRoom(Player player, double type){
        List<Room> list = notFullRoom.get(type);
        if (list == null) {
            list = new ArrayList<>();
        }
        Room room;
        if (list.size() > 0) {
            room = list.get(0);
        } else {//新建room
            room = new RoomDouDiZhuGold();
            room.personNumber = PERSONNUM;
            room.roomId = Room.getRoomIdStr(genRoomId());
            room.createNeedMoney = 0;
            room.init(1, -1);
            room.setCreateType(ROOM_CREATE_TYPE_GOLD);
            room.setGoldRoomType(type);
            list.add(room);
        }

        notFullRoom.put(type, list);

        int rtn = room.joinRoom(player);
        //加入房间列表
        GameManager.getInstance().rooms.put(room.getRoomId(), room);

        //如果已经满了
        if (room.getUserMap().size() >= room.getPersonNumber()) {
            //删掉未满的
            removeRoomFromMap(notFullRoom,room);
            //加入已满的
            addRoom2Map(fullRoom, room);
        }
        if (rtn != 0) {
            return rtn;
        }


        player.sendMsg("roomService","joinRoomQuick",0);
        return 0;
    }



    public static void addRoom2Map(Map<Double, List<Room>> map,Room room){
        List<Room> list = map.get(room.goldRoomType);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(room);
    }

    public static void removeRoomFromMap(Map<Double, List<Room>> map,Room room){
        List<Room> list = map.get(room.goldRoomType);
        if (list != null) {
            list.remove(room);
        }
    }

    //房间不足人数，添加到房间  超过房间人数(4),其他返回房间人数
    public int addRoom(int gameNumber, int multiple, double roomType, Player player) {
        if (this.getRoomLock().containsKey(roomType)) {
            int personNumber = this.getRoomLock().get(roomType).getUsers().size();
            if (personNumber >= GAME_PERSON_NUMBER) {
                this.getRoomLock().remove(roomType);
                return 4;
            } else {
                RoomDouDiZhuGold room = this.getRoomLock().get(roomType);
                room.roomAddUser(player);
                this.getRoomLock().put(roomType, room);
                personNumber = room.getUsers().size();
                if (personNumber == 3) {
                    GameManager.getInstance().rooms.put(room.roomId, room);
                    for (Long userId : this.roomLock.get(roomType).getUsers()) {
                        GameManager.getInstance().userRoom.put(userId, room.roomId);
                    }
                    this.getRoomLock().remove(roomType);
                }
                return personNumber;
            }
        } else {//不存在该类型的房间,加入人数为1
            RoomDouDiZhuGold room = new RoomDouDiZhuGold();
            room.personNumber = PERSONNUM;
            room.roomId = room.getRoomIdStr(genRoomId());
            room.init(gameNumber, multiple);
            room.roomAddUser(player);
            this.getRoomLock().put(roomType, room);
            return 1;
        }
    }

    //房间不足人数，删除房间类型，其他返回房间人数，不存在该房间类型或者user，返回-1
    public int quitRoom(double roomType, long userId) {
        if (this.getRoomLock().containsKey(roomType)) {
            GameManager.getInstance().getUserRoom().remove(userId);
            int personNumber = this.getRoomLock().get(roomType).getUsers().size();
            if (personNumber == 1 && this.getRoomLock().get(roomType).getUsers().contains(userId)) {
                this.getRoomLock().remove(roomType);
                return 0;
            } else {
                this.getRoomLock().get(roomType).getUsers().remove(userId);
                this.getRoomLock().put(roomType, this.getRoomLock().get(roomType));
                return personNumber - 1;
            }
        } else {//不存在该类型的房间,加入人数为1
            return -1;
        }
    }


    public Map<Double, List<Room>> getFullRoom() {
        return fullRoom;
    }

    public GoldRoomPool setFullRoom(Map<Double, List<Room>> fullRoom) {
        this.fullRoom = fullRoom;
        return this;
    }

    public Map<Double, List<Room>> getNotFullRoom() {
        return notFullRoom;
    }

    public GoldRoomPool setNotFullRoom(Map<Double, List<Room>> notFullRoom) {
        this.notFullRoom = notFullRoom;
        return this;
    }
}
