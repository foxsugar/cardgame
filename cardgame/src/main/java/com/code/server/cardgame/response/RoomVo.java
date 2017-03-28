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

    protected Map<Long, Integer> userStatus = new HashMap<>();//用户状态
    protected List<UserVo> userList = new ArrayList<>();//用户列表
    protected Map<Long, Double> userScores = new HashMap<>();


    public RoomVo(){}

    public RoomVo(Room roomDouDiZhu, Player player){
        this.roomId = roomDouDiZhu.getRoomId();
        this.multiple = roomDouDiZhu.getMultiple();
        this.gameNumber = roomDouDiZhu.getGameNumber();
        this.createUser = roomDouDiZhu.getCreateUser();
        this.userStatus.putAll(roomDouDiZhu.getUserStatus());
        this.userScores.putAll(roomDouDiZhu.getUserScores());
        this.curGameNumber = roomDouDiZhu.getCurGameNumber();
        for(long uid : roomDouDiZhu.getUsers()){
            userList.add(GameManager.getUserVo(roomDouDiZhu.getUserMap().get(uid)));
        }

        this.game = GameVo.getGameVo(roomDouDiZhu.getGame(),player.getUserId());

    }


}
