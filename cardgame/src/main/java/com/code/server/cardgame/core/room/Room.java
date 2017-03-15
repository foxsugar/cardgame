package com.code.server.cardgame.core.room;

import com.code.server.cardgame.timer.TimerNode;
import com.code.server.db.model.User;

import java.util.*;

/**
 * Created by sunxianping on 2017/3/14.
 */
public class Room {
    protected String roomId;

    protected int createNeedMoney;
    protected static Random random = new Random();

    protected Map<Long, Integer> userStatus = new HashMap<>();//用户状态
    protected List<Long> users = new ArrayList<>();//用户列表
    protected Map<Long, Integer> userScores = new HashMap<>();
    protected Map<Long,User> userMap = new HashMap<>();//用户列表

    protected int multiple;//倍数
    protected int gameNumber;
    protected int curGameNumber = 1;
    protected int personNumber;
    protected long createUser;
    protected int bankerId;//庄家

    protected boolean isInGame;

    protected boolean isHasDissolutionRequest;
    protected transient TimerNode timerNode;


    public String getRoomId() {
        return roomId;
    }

    public Room setRoomId(String roomId) {
        this.roomId = roomId;
        return this;
    }

    public int getCreateNeedMoney() {
        return createNeedMoney;
    }

    public Room setCreateNeedMoney(int createNeedMoney) {
        this.createNeedMoney = createNeedMoney;
        return this;
    }

    public static Random getRandom() {
        return random;
    }

    public static void setRandom(Random random) {
        Room.random = random;
    }

    public Map<Long, Integer> getUserStatus() {
        return userStatus;
    }

    public Room setUserStatus(Map<Long, Integer> userStatus) {
        this.userStatus = userStatus;
        return this;
    }

    public List<Long> getUsers() {
        return users;
    }

    public Room setUsers(List<Long> users) {
        this.users = users;
        return this;
    }

    public Map<Long, Integer> getUserScores() {
        return userScores;
    }

    public Room setUserScores(Map<Long, Integer> userScores) {
        this.userScores = userScores;
        return this;
    }

    public Map<Long, User> getUserMap() {
        return userMap;
    }

    public Room setUserMap(Map<Long, User> userMap) {
        this.userMap = userMap;
        return this;
    }

    public int getMultiple() {
        return multiple;
    }

    public Room setMultiple(int multiple) {
        this.multiple = multiple;
        return this;
    }

    public int getGameNumber() {
        return gameNumber;
    }

    public Room setGameNumber(int gameNumber) {
        this.gameNumber = gameNumber;
        return this;
    }

    public int getCurGameNumber() {
        return curGameNumber;
    }

    public Room setCurGameNumber(int curGameNumber) {
        this.curGameNumber = curGameNumber;
        return this;
    }

    public int getPersonNumber() {
        return personNumber;
    }

    public Room setPersonNumber(int personNumber) {
        this.personNumber = personNumber;
        return this;
    }

    public long getCreateUser() {
        return createUser;
    }

    public Room setCreateUser(long createUser) {
        this.createUser = createUser;
        return this;
    }

    public int getBankerId() {
        return bankerId;
    }

    public Room setBankerId(int bankerId) {
        this.bankerId = bankerId;
        return this;
    }

    public boolean isInGame() {
        return isInGame;
    }

    public Room setInGame(boolean inGame) {
        isInGame = inGame;
        return this;
    }

    public boolean isHasDissolutionRequest() {
        return isHasDissolutionRequest;
    }

    public Room setHasDissolutionRequest(boolean hasDissolutionRequest) {
        isHasDissolutionRequest = hasDissolutionRequest;
        return this;
    }

    public TimerNode getTimerNode() {
        return timerNode;
    }

    public Room setTimerNode(TimerNode timerNode) {
        this.timerNode = timerNode;
        return this;
    }
}
