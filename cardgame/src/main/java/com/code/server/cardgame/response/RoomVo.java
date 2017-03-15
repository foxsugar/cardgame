package com.code.server.cardgame.response;

import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.room.Room;
import com.code.server.cardgame.core.room.RoomDouDiZhu;
import com.code.server.db.model.User;
import com.code.server.gamedata.UserVo;

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

    protected Map<Long, Integer> userStatus = new HashMap<>();//用户状态
    protected List<UserVo> userList = new ArrayList<>();//用户列表
    protected Map<Long, Integer> userScores = new HashMap<>();


    public RoomVo(){}

    public RoomVo(Room roomDouDiZhu){
        this.roomId = roomDouDiZhu.getRoomId();
        this.multiple = roomDouDiZhu.getMultiple();
        this.gameNumber = roomDouDiZhu.getGameNumber();
        this.createUser = roomDouDiZhu.getCreateUser();
        this.userStatus.putAll(roomDouDiZhu.getUserStatus());
        this.userScores.putAll(roomDouDiZhu.getUserScores());
        for(User user : roomDouDiZhu.getUserMap().values()){
            userList.add(GameManager.getUserVo(user));
        }

        this.game = new GameVo();

    }


}
