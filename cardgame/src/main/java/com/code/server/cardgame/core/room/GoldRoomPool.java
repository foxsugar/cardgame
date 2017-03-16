package com.code.server.cardgame.core.room;

import com.code.server.cardgame.core.GameManager;
import org.apache.commons.collections.map.HashedMap;

import java.util.Map;

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

    private GoldRoomPool(){}

    private static GoldRoomPool instance;

    public static GoldRoomPool getInstance() {
        if (instance == null) {
            instance = new GoldRoomPool();
        }
        return instance;
    }

    public static final int GAME_PERSON_NUMBER = 3;//游戏人数
    private Map<Double, GoldRoomDouDiZhu> roomLock = new HashedMap();

    public Map<Double, GoldRoomDouDiZhu> getRoomLock() {
        return roomLock;
    }

    public void setRoomLock(Map<Double, GoldRoomDouDiZhu> roomLock) {
        this.roomLock = roomLock;
    }

    //房间不足人数，添加到房间  超过房间人数(4),其他返回房间人数
    public int addRoom(int gameNumber,int multiple,double roomType,Player player){
        if(this.getRoomLock().containsKey(roomType)){
            int personNumber = this.getRoomLock().get(roomType).getUsers().size();
            if(personNumber >= GAME_PERSON_NUMBER){
                this.getRoomLock().remove(roomType);
                return 4;
            }else{
                GoldRoomDouDiZhu room =this.getRoomLock().get(roomType);
                room.roomAddUser(player);
                this.getRoomLock().put(roomType,room);
                personNumber =room.getUsers().size();
                if(personNumber==3){
                    GameManager.getInstance().rooms.put(room.roomId, room);
                    for (Long userId:this.roomLock.get(roomType).getUsers()) {
                        GameManager.getInstance().userRoom.put(userId, room.roomId);
                    }
                    this.getRoomLock().remove(roomType);
                }
                return personNumber;
            }
        }else {//不存在该类型的房间,加入人数为1
            GoldRoomDouDiZhu room = new GoldRoomDouDiZhu();
            room.personNumber = PERSONNUM;
            room.roomId = room.getRoomIdStr(genRoomId());
            room.init(gameNumber,multiple);
            room.roomAddUser(player);
            this.getRoomLock().put(roomType,room);
            return 1;
        }
    }

    //房间不足人数，删除房间类型，其他返回房间人数，不存在该房间类型或者user，返回-1
    public int quitRoom(double roomType,long userId){
        if(this.getRoomLock().containsKey(roomType)){
            GameManager.getInstance().getUserRoom().remove(userId);
            int personNumber = this.getRoomLock().get(roomType).getUsers().size();
            if(personNumber == 1 && this.getRoomLock().get(roomType).getUsers().contains(userId)){
                this.getRoomLock().remove(roomType);
                return 0;
            }else{
                this.getRoomLock().get(roomType).getUsers().remove(userId);
                this.getRoomLock().put(roomType,this.getRoomLock().get(roomType));
                return personNumber-1;
            }
        }else {//不存在该类型的房间,加入人数为1
            return -1;
        }
    }
}
