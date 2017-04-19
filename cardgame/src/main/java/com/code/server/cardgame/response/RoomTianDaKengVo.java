package com.code.server.cardgame.response;

import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.core.room.Room;
import com.code.server.cardgame.core.room.RoomTanDaKeng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ClarkKent on 2017/3/15.
 */
public class RoomTianDaKengVo {


    protected String roomId;
    protected double multiple;//倍数
    protected int gameNumber;
    protected long createUser;
    private GameVo game;
    private int curGameNumber;

    private boolean isLastDraw = false;//是否平局
    private int drawForLeaveChip = 0;//平局留下筹码

    protected Map<Long, Integer> userStatus = new HashMap<>();//用户状态
    protected List<UserVo> userList = new ArrayList<>();//用户列表
    protected Map<Long, Double> userScores = new HashMap<>();


    public RoomTianDaKengVo(){}

    public RoomTianDaKengVo(RoomTanDaKeng roomTianDaKeng, Player player){
        this.roomId = roomTianDaKeng.getRoomId();
        this.multiple = roomTianDaKeng.getMultiple();
        this.gameNumber = roomTianDaKeng.getGameNumber();
        this.createUser = roomTianDaKeng.getCreateUser();
        this.userStatus.putAll(roomTianDaKeng.getUserStatus());
        this.userScores.putAll(roomTianDaKeng.getUserScores());
        this.curGameNumber = roomTianDaKeng.getCurGameNumber();
        this.isLastDraw = roomTianDaKeng.isLastDraw();
        this.drawForLeaveChip = roomTianDaKeng.getDrawForLeaveChip();

        for(long uid : roomTianDaKeng.getUsers()){
            userList.add(GameManager.getUserVo(roomTianDaKeng.getUserMap().get(uid)));
        }

        this.game = GameTianDaKengVo.getGameTianDaKengVo(roomTianDaKeng.getGame(),player.getUserId());

    }


}
