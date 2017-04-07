package com.code.server.cardgame.response;

import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.core.room.Room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sunxianping on 2017/3/15.
 */
public class RoomVo {


    protected String roomId;
    protected int multiple;//倍数
    protected int gameNumber;
    protected long createUser;
    private GameVo game;
    private int curGameNumber;
    protected int createType;
    protected double goldRoomType;

    protected Map<Long, Integer> userStatus = new HashMap<>();//用户状态
    protected List<UserVo> userList = new ArrayList<>();//用户列表
    protected Map<Long, Double> userScores = new HashMap<>();


    public RoomVo(){}

    public RoomVo(Room room, Player player){
        this.createType = room.getCreateType();
        this.roomId = room.getRoomId();
        this.multiple = room.getMultiple();
        this.gameNumber = room.getGameNumber();
        this.createUser = room.getCreateUser();
        this.userStatus.putAll(room.getUserStatus());
        this.userScores.putAll(room.getUserScores());
        this.curGameNumber = room.getCurGameNumber();
        this.goldRoomType = room.getGoldRoomType();
        for(long uid : room.getUsers()){
            userList.add(GameManager.getUserVo(room.getUserMap().get(uid)));
        }

        this.game = GameVo.getGameVo(room.getGame(),player.getUserId());

    }


}
