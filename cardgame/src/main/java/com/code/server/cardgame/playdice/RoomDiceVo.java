package com.code.server.cardgame.playdice;

import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.response.GameTianDaKengVo;
import com.code.server.cardgame.response.GameVo;
import com.code.server.cardgame.response.UserVo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class RoomDiceVo {

    protected String roomId;
    protected int cricle;
    private int personNumber;
    protected int isSelf;

    protected long createUser;
    private GameVo game;
    private int curCricleNumber;
    private Long curBanker;

    protected Map<Long, Integer> userStatus = new HashMap<>();//用户状态
    protected List<UserVo> userList = new ArrayList<>();//用户列表
    protected Map<Long, Double> userScores = new HashMap<>();


    public RoomDiceVo(){}

    public RoomDiceVo(RoomDice roomDice, Player player){
        this.roomId = roomDice.getRoomId();
        this.cricle = roomDice.getCricle();
        this.personNumber = roomDice.getPersonNumber();
        this.isSelf = roomDice.getIsSelf();

        this.createUser = roomDice.getCreateUser();
        this.curCricleNumber = roomDice.getCurCricleNumber();
        this.curBanker = roomDice.getCurBanker();

        this.userStatus.putAll(roomDice.getUserStatus());
        this.userScores.putAll(roomDice.getUserScores());

        for(long uid : roomDice.getUsers()){
            userList.add(GameManager.getUserVo(roomDice.getUserMap().get(uid)));
        }

        if(roomDice.getGame()!=null){
            this.game = GameTianDaKengVo.getGameTianDaKengVo(roomDice.getGame(),player.getUserId());
        }

    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int getCricle() {
        return cricle;
    }

    public void setCricle(int cricle) {
        this.cricle = cricle;
    }

    public int getPersonNumber() {
        return personNumber;
    }

    public void setPersonNumber(int personNumber) {
        this.personNumber = personNumber;
    }

    public int getIsSelf() {
        return isSelf;
    }

    public void setIsSelf(int isSelf) {
        this.isSelf = isSelf;
    }

    public long getCreateUser() {
        return createUser;
    }

    public void setCreateUser(long createUser) {
        this.createUser = createUser;
    }

    public GameVo getGame() {
        return game;
    }

    public void setGame(GameVo game) {
        this.game = game;
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

    public Map<Long, Integer> getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(Map<Long, Integer> userStatus) {
        this.userStatus = userStatus;
    }

    public List<UserVo> getUserList() {
        return userList;
    }

    public void setUserList(List<UserVo> userList) {
        this.userList = userList;
    }

    public Map<Long, Double> getUserScores() {
        return userScores;
    }

    public void setUserScores(Map<Long, Double> userScores) {
        this.userScores = userScores;
    }
}
