package com.code.server.cardgame.core.room;

import org.apache.commons.collections.map.HashedMap;

import java.util.Map;

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

    private GoldRoomPool(){}

    private static GoldRoomPool instance;

    public static GoldRoomPool getInstance() {
        if (instance == null) {
            instance = new GoldRoomPool();
        }
        return instance;
    }

    public static final int GAME_PERSON_NUMBER = 3;//游戏人数
    private Map<Double, Room> roomLock = new HashedMap();

    public Map<Double, Room> getRoomLock() {
        return roomLock;
    }

    public void setRoomLock(Map<Double, Room> roomLock) {
        this.roomLock = roomLock;
    }

    //房间不足人数，添加到房间  超过房间人数(4),其他返回房间人数
    public int addRoom(double roomType,int userId){
        if(this.getRoomLock().containsKey(roomType)){
            int personNumber = this.getRoomLock().get(roomType).getUsers().size();
            if(personNumber >= GAME_PERSON_NUMBER-1){
                this.getRoomLock().remove(roomType);
                return 4;
            }else{
                this.getRoomLock().get(roomType).getUsers().add(userId);
                this.getRoomLock().put(roomType,this.getRoomLock().get(roomType));
                personNumber = this.getRoomLock().get(roomType).getUsers().size();
                if(personNumber==3){
                    this.getRoomLock().remove(roomType);
                }
                return personNumber;
            }
        }else {//不存在该类型的房间,加入人数为1
            Room room = new Room();
            room.getUsers().add(userId);
            this.getRoomLock().put(roomType,room);
            return 1;
        }
    }



}
