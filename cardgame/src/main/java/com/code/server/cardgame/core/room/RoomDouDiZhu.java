package com.code.server.cardgame.core.room;

import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.core.game.GameDouDiZhu;
import com.code.server.cardgame.response.*;
import com.code.server.cardgame.timer.GameTimer;
import com.code.server.cardgame.timer.ITimeHandler;
import com.code.server.cardgame.timer.TimerNode;
import com.code.server.db.model.User;
import com.google.gson.Gson;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sunxianping on 2017/3/13.
 */
public class RoomDouDiZhu {


    private static final Logger logger = Logger.getLogger("game");

    public static final int STATUS_JOIN = 0;
    public static final int STATUS_READY = 1;
    public static final int STATUS_IN_GAME = 2;
    public static final int STATUS_DISSOLUTION = 3;
    public static final int STATUS_AGREE_DISSOLUTION = 4;

    public static final long FIVE_MIN = 1000L * 60 * 5;

    protected String roomId;

    protected int createNeedMoney;

    protected Map<Long, Integer> userStatus = new HashMap<>();//用户状态
    protected List<Long> users = new ArrayList<>();//用户列表
    protected Map<Long, Integer> userScores = new HashMap<>();
    protected Map<Long,User> userMap = new HashMap<>();//用户列表

    protected int multiple;//倍数
    protected int gameNumber;
    protected int curGameNumber = 1;
    protected int personNumber;
    protected int createUser;
    protected int bankerId;//庄家

    protected boolean isInGame;

    protected boolean isHasDissolutionRequest;
    protected transient TimerNode timerNode;

    private GameDouDiZhu game;


    protected String gameType;//麻将项目名称





    protected boolean isCanDissloution = false;



    public void init(String roomId, int userId, String modeTotal, String mode, int multiple, int gameNumber, int createUser, int bankerId) {
        this.roomId = roomId;
        this.multiple = multiple;
        this.gameNumber = gameNumber;
        this.personNumber = personNumber;
        this.createUser = createUser;
        this.bankerId = bankerId;
        this.isInGame = false;

        //todo
        this.createNeedMoney = 1;
    }



    public int joinRoom(Player player,long userId) {
        if (this.users.contains(userId)) {
            return ErrorCode.CANNOT_CREATE_ROOM_USER_HAS_IN_ROOM;

        }
        if (this.users.size() >= this.personNumber) {
            return ErrorCode.CANNOT_JOIN_ROOM_IS_FULL;

        }
        if (!isCanJoinCheckMoney(player)) {
            return ErrorCode.CANNOT_JOIN_ROOM_NO_MONEY;
        }


        this.users.add(userId);
        this.userStatus.put(userId, 0);
        this.userScores.put(userId, 0);
        //加进玩家-房间映射表
        GameManager.getInstance().getUserRoom().put(userId, roomId);
        return -2;
    }

    protected boolean isCanJoinCheckMoney(Player player) {
        if (player.getUserId() == createUser) {

            User user = player.getUser();
            if (user.getMoney() < createNeedMoney) {
                return false;
            }
        }
        return true;
    }


    public int quitRoom(long userId) {
        if (!this.users.contains(userId)) {
            return ErrorCode.CANNOT_QUIT_ROOM_NOT_EXIST;

        }

        if (isInGame) {
            return ErrorCode.CANNOT_QUIT_ROOM_IS_IN_GAME;
        }

        this.userStatus.remove(userId);
        this.users.remove(userId);
        this.userScores.remove(userId);
        //删除玩家房间映射关系
        GameManager.getInstance().getUserRoom().remove(userId);
        return -2;
    }


    public int getReady(Player player,long userId) {
        if (!this.users.contains(userId)) {
            return ErrorCode.CANNOT_FIND_THIS_USER;

        }
        if (isInGame) {
            return  ErrorCode.CANNOT_FIND_THIS_USER;
        }

        this.userStatus.put(userId, STATUS_READY);

        int readyNum = 0;
        for (Map.Entry<Long, Integer> entry : this.userStatus.entrySet()) {
            if (entry.getValue() == STATUS_READY) {
                readyNum += 1;
            }
        }

        //通知客户端谁是否准备
        Map<String, Integer> userStatus = new HashMap<>();
        for (Long i : this.userStatus.keySet()) {
            userStatus.put(i + "", this.userStatus.get(i));
        }
        NoticeReady noticeReady = new NoticeReady();
        noticeReady.setUserStatus(userStatus);
        JSONObject getReadyResult = new JSONObject();
        getReadyResult.put("service", "roomService");
        getReadyResult.put("method", "noticeReady");
        getReadyResult.put("params", noticeReady.toJSONObject());
        getReadyResult.put("code", "0");
        Player.sendMsg2Player(getReadyResult, this.users);

        //开始游戏
        if (readyNum >= personNumber) {
            startGame();
        }
    }


    public void addUserSocre(long userId, int score) {
        if (!userScores.containsKey(userId)) {
            logger.error("===设置分数时出错 userId = "+userId +"users: "+userScores.toString());
            return;
        }
        int s = userScores.get(userId);
        userScores.put(userId, s + score);
    }

    public void clearReadyStatus() {
        this.setGame(null);
        this.setInGame(false);
        for (Map.Entry<Long, Integer> entry : this.userStatus.entrySet()) {
            entry.setValue(STATUS_JOIN);
        }
        this.curGameNumber += 1;
//        //每局的庄家
//        this.bankerMap.put(curGameNumber, bankerId);
//
//        //
//        saveRecode();
    }








    private void startGame() {
        this.isInGame = true;
        GameDouDiZhu game = new GameDouDiZhu();


        //扣钱
        if (curGameNumber == 1) {
            spendMoney();
        }
        gameInfo.init(game.getId(), this.bankerId, this.users, this, roomDao, userRecodeDao, userDao, gameDao);
        gameInfo.fapai(this.serverContext);
        this.gameInfo = gameInfo;
        this.game = game;
        game.setGameInfo(gameInfo);


        gameDao.saveGame(game);
        GameManager.getInstance().addGame(game);


        //通知其他人游戏已经开始
        CardEntity cardBegin = new CardEntity();
        cardBegin.setCurrentUserId(this.getBankerId() + "");
        JSONObject beginResult = new JSONObject();
        beginResult.put("service", "gameService");
        beginResult.put("method", "gameBegin");
        beginResult.put("params", game.toJSONObjectOfGameBegin());
        beginResult.put("code", "0");
        serverContext.sendToOnlinePlayer(beginResult, this.getUsers());
        pushScoreChange();
    }

    public void pushScoreChange() {
        Gson gson = new Gson();
        String json = gson.toJson(userScores);
        JSONObject beginResult = new JSONObject();
        beginResult.put("service", "gameService");
        beginResult.put("method", "scoreChange");
        beginResult.put("params", json);
        beginResult.put("code", "0");
        Player.sendMsg2Player(beginResult,this.getUsers());
    }


    public void dissolution(long userId, boolean agreeOrNot) {
        if (!this.users.contains(userId)) {
            return ErrorCode.CANNOT_FIND_THIS_USER

        }

        this.userStatus.put(userId, agreeOrNot ? STATUS_AGREE_DISSOLUTION : STATUS_DISSOLUTION);

        //第一个点解散
        if (agreeOrNot && !isHasDissolutionRequest) {
            isCanDissloution = true;
            this.isHasDissolutionRequest = true;
            //第一次申请 五分钟后解散
            long start = System.currentTimeMillis();
            TimerNode node = new TimerNode(start, FIVE_MIN, false, ()-> {
                @Override
                public void fire() {

                        if (isCanDissloution) {
                            dissolutionRoom();
                            logger.info("===2定时解散 roomId: "+roomId);
                        }

                }
            });
            this.timerNode = node;
            GameTimer.getInstance().addTimerNode(node);
        }


        ArrayList<AnswerUser> answerUsers = new ArrayList<>();
        for (int i = 0; i < this.users.size(); i++) {
            AnswerUser answerUser = new AnswerUser();
            answerUser.setUserId(this.users.get(i) + "");
            if (this.userStatus.get(this.users.get(i)) == STATUS_DISSOLUTION) {
                answerUser.setAnswer("3");
            } else if (this.userStatus.get(this.users.get(i)) == STATUS_AGREE_DISSOLUTION) {
                answerUser.setAnswer("2");
            } else {
                answerUser.setAnswer("1");
            }
            answerUsers.add(answerUser);
        }

        AskQuitRoom accept = new AskQuitRoom();
        accept.setUserId(userId + "");
        accept.setAnswerList(answerUsers);

        JSONObject noticeResult = new JSONObject();
        noticeResult.put("service", "roomService");
        noticeResult.put("method", "noticeAnswerIfDissolveRoom");
        noticeResult.put("params", accept.toJSONObject());
        noticeResult.put("code", "0");

        Player.sendMsg2Player(noticeResult, this.users);


        int agreeNum = 0;
        int disAgreeNum = 0;
        for (int status : userStatus.values()) {
            if (status == STATUS_AGREE_DISSOLUTION) {
                agreeNum += 1;
            }
            if (status == STATUS_DISSOLUTION) {
                disAgreeNum += 1;
            }
        }

        //同意解散
        if (agreeNum >= personNumber - 1) {
            try {
                GameTimer.getInstance().removeNode(timerNode);
                dissolutionRoom(userDao, userRecodeDao, serverContext);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        //不同意的人数大于2 解散取消
        if (disAgreeNum >= 1) {
            for (Map.Entry<Long, Integer> entry : userStatus.entrySet()) {
                //回到游戏状态
                entry.setValue(STATUS_IN_GAME);
                this.isHasDissolutionRequest = false;
                GameTimer.getInstance().removeNode(timerNode);
            }
        }
    }

    private void dissolutionRoom(){



        GameManager.getInstance().rooms.remove(this.roomId);

        // 结果类
        ArrayList<UserOfResult> userOfResultList = new ArrayList<>();

        long time = System.currentTimeMillis();

        for(User user : this.userMap.values()){
            UserOfResult resultObj = new UserOfResult();
            User eashUser = userMap(this.users.get(i));
            eashUser.setRoomId("0");
            eashUser.setSeatId("0");
            userDao.saveUser(eashUser);

            try {
                resultObj.setUsername(URLDecoder.decode(eashUser.getUsername(), "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            resultObj.setImage(eashUser.getImage());
            resultObj.setScores(this.userScores.get(this.users.get(i)) + "");
            resultObj.setUserId(user.getUserId());
            resultObj.setTime(time);




            userOfResultList.add(resultObj);
            //删除映射关系
            GameManager.getInstance().getUserRoom().remove(users.get(i));
        }



        boolean isChange = scoreIsChange();
        if (this.isInGame && this.curGameNumber == 1 && !isChange) {
            drawBack();
        }



        this.isInGame = false;
        // 存储返回
        GameOfResult gameOfResult = new GameOfResult();
        gameOfResult.setUserList(userOfResultList);

        JSONObject noticeEndResult = new JSONObject();
        noticeEndResult.put("service", "gameService");
        noticeEndResult.put("method", "askNoticeDissolutionResult");
        noticeEndResult.put("params", gameOfResult.toJSONObject());
        noticeEndResult.put("code", "0");
        Player.sendMsg2Player(noticeEndResult, this.users);

    }

    public boolean scoreIsChange() {
        for (int score : userScores.values()) {
            if (score != 0) {
                return true;
            }
        }
        return false;
    }

    public void drawBack() {
        User user = userMap.get(this.createUser);
        if (user != null) {
            user.setMoney(user.getMoney() + createNeedMoney);
        }
    }

    public void spendMoney() {
        User user = userMap.get(this.createUser);
        if (user != null) {
            user.setMoney(user.getMoney() - createNeedMoney);
        }
    }


    public String getRoomId() {
        return roomId;
    }

    public RoomDouDiZhu setRoomId(String roomId) {
        this.roomId = roomId;
        return this;
    }

    public int getCreateNeedMoney() {
        return createNeedMoney;
    }

    public RoomDouDiZhu setCreateNeedMoney(int createNeedMoney) {
        this.createNeedMoney = createNeedMoney;
        return this;
    }

    public Map<Long, Integer> getUserStatus() {
        return userStatus;
    }

    public RoomDouDiZhu setUserStatus(Map<Long, Integer> userStatus) {
        this.userStatus = userStatus;
        return this;
    }

    public List<Long> getUsers() {
        return users;
    }

    public RoomDouDiZhu setUsers(List<Long> users) {
        this.users = users;
        return this;
    }

    public Map<Long, Integer> getUserScores() {
        return userScores;
    }

    public RoomDouDiZhu setUserScores(Map<Long, Integer> userScores) {
        this.userScores = userScores;
        return this;
    }

    public Map<Long, User> getUserMap() {
        return userMap;
    }

    public RoomDouDiZhu setUserMap(Map<Long, User> userMap) {
        this.userMap = userMap;
        return this;
    }

    public int getMultiple() {
        return multiple;
    }

    public RoomDouDiZhu setMultiple(int multiple) {
        this.multiple = multiple;
        return this;
    }

    public int getGameNumber() {
        return gameNumber;
    }

    public RoomDouDiZhu setGameNumber(int gameNumber) {
        this.gameNumber = gameNumber;
        return this;
    }

    public int getCurGameNumber() {
        return curGameNumber;
    }

    public RoomDouDiZhu setCurGameNumber(int curGameNumber) {
        this.curGameNumber = curGameNumber;
        return this;
    }

    public int getPersonNumber() {
        return personNumber;
    }

    public RoomDouDiZhu setPersonNumber(int personNumber) {
        this.personNumber = personNumber;
        return this;
    }

    public int getCreateUser() {
        return createUser;
    }

    public RoomDouDiZhu setCreateUser(int createUser) {
        this.createUser = createUser;
        return this;
    }

    public int getBankerId() {
        return bankerId;
    }

    public RoomDouDiZhu setBankerId(int bankerId) {
        this.bankerId = bankerId;
        return this;
    }

    public boolean isInGame() {
        return isInGame;
    }

    public RoomDouDiZhu setInGame(boolean inGame) {
        isInGame = inGame;
        return this;
    }

    public boolean isHasDissolutionRequest() {
        return isHasDissolutionRequest;
    }

    public RoomDouDiZhu setHasDissolutionRequest(boolean hasDissolutionRequest) {
        isHasDissolutionRequest = hasDissolutionRequest;
        return this;
    }

    public TimerNode getTimerNode() {
        return timerNode;
    }

    public RoomDouDiZhu setTimerNode(TimerNode timerNode) {
        this.timerNode = timerNode;
        return this;
    }

    public GameDouDiZhu getGame() {
        return game;
    }

    public RoomDouDiZhu setGame(GameDouDiZhu game) {
        this.game = game;
        return this;
    }

    public String getGameType() {
        return gameType;
    }

    public RoomDouDiZhu setGameType(String gameType) {
        this.gameType = gameType;
        return this;
    }

    public boolean isCanDissloution() {
        return isCanDissloution;
    }

    public RoomDouDiZhu setCanDissloution(boolean canDissloution) {
        isCanDissloution = canDissloution;
        return this;
    }
}
