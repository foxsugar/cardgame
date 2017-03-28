package com.code.server.cardgame.core.room;

import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.core.game.GameDouDiZhu;
import com.code.server.cardgame.core.game.GameTianDaKeng;
import com.code.server.cardgame.encoding.Notice;
import com.code.server.cardgame.response.*;
import com.code.server.cardgame.timer.GameTimer;
import com.code.server.cardgame.timer.TimerNode;
import com.code.server.db.model.User;
import com.code.server.gamedata.UserVo;
import com.google.gson.Gson;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * Created by sunxianping on 2017/3/13.
 */
public class RoomTanDaKeng extends Room{


    private static final Logger logger = Logger.getLogger("game");

    public static final int STATUS_JOIN = 0;
    public static final int STATUS_READY = 1;
    public static final int STATUS_IN_GAME = 2;
    public static final int STATUS_DISSOLUTION = 3;
    public static final int STATUS_AGREE_DISSOLUTION = 4;

    public static final long FIVE_MIN = 1000L * 60 * 5;



    protected String roomId;

    protected int createNeedMoney;
    protected static Random random = new Random();

    protected Map<Long, Integer> userStatus = new HashMap<>();//用户状态
    protected List<Long> users = new ArrayList<>();//用户列表
    protected Map<Long, Integer> userScores = new HashMap<>();
    protected Map<Long,User> userMap = new HashMap<>();//用户列表

    protected double roomType;//几倍房
    protected int gameNumber;
    protected int curGameNumber = 1;
    protected long createUser;
    protected int bankerId;//庄家

    protected boolean isInGame;

    protected boolean isHasDissolutionRequest;
    protected transient TimerNode timerNode;
    private GameTianDaKeng game;
    protected int personNumber;
    protected String gameType;//项目名称

    protected boolean isCanDissloution = false;



    public static int createRoom(Player player,int gameNumber,double roomType,int personNumber){
        if(GameManager.getInstance().userRoom.containsKey(player.getUserId())){
            return ErrorCode.CANNOT_CREATE_ROOM_ROLE_HAS_IN_ROOM;
        }
        int needMoney = getNeedMoney(gameNumber);
        if (player.getUser().getMoney() < needMoney) {
            return ErrorCode.CANNOT_CREATE_ROOM_MONEY;
        }
        RoomTanDaKeng room = new RoomTanDaKeng();
        room.personNumber = personNumber;

        room.roomId = getRoomIdStr(genRoomId());
        room.createUser = player.getUserId();
        room.init(gameNumber,roomType);
        //房间加入列表
        room.roomAddUser(player);
        GameManager.getInstance().roomsOfTanDaKeng.put(room.roomId, room);

        player.sendMsg(new ResponseVo("roomService","createRoom",new RoomTianDaKengVo(room,player)));

        return 0;
    }

    protected static String getRoomIdStr(int roomId){
        String s = "000000" + roomId;
        int len = s.length();
        return s.substring(len-6,len);
    }

    public static void main(String[] args) {
        System.out.println(getRoomIdStr(99999));
    }
    protected static int genRoomId(){

        while (true) {
            int id = random.nextInt(999999);

            boolean isHas = GameManager.getInstance().rooms.containsKey(""+id);
            if (!isHas) {
                return id;
            }

        }
    }

    public static int getNeedMoney(int gameNumber) {
        if (gameNumber == 32) {
            return 1;
        } else {
            return 1;
        }

    }

    public void init(int gameNumber, double roomType) {
        this.gameNumber = gameNumber;
        this.roomType = roomType;
        this.isInGame = false;
    }



    public int joinRoom(Player player) {
        long userId = player.getUserId();
        if (this.users.contains(userId)) {
            return ErrorCode.CANNOT_CREATE_ROOM_USER_HAS_IN_ROOM;

        }
        if (this.users.size() >= this.personNumber) {
            return ErrorCode.CANNOT_JOIN_ROOM_IS_FULL;

        }
        if (!isCanJoinCheckMoney(player)) {
            return ErrorCode.CANNOT_JOIN_ROOM_NO_MONEY;
        }



        roomAddUser(player);
        //加进玩家-房间映射表
        GameManager.getInstance().getUserRoom().put(userId, roomId);
        noticeJoinRoom(player);

        return 0;
    }

    protected void roomAddUser(Player player) {
        User user = player.getUser();
        long userId = user.getUserId();
        this.users.add(userId);
        this.userStatus.put(userId, 0);
        this.userScores.put(userId, 0);
        this.userMap.put(userId, player.getUser());
        GameManager.getInstance().getUserRoom().put(player.getUserId(),roomId);
    }

    protected void roomRemoveUser(Player player) {
        User user = player.getUser();
        long userId = user.getUserId();
        this.users.remove(userId);
        this.userStatus.remove(userId);
        this.userScores.remove(userId);
        this.userMap.remove(userId);
        GameManager.getInstance().getUserRoom().remove(userId);
    }


    private void noticeJoinRoom(Player player){
        List<UserVo> usersList = new ArrayList<>();
        UserOfRoom userOfRoom = new UserOfRoom();
        int readyNumber = 0;
        for (long userId : users) {
            User user = this.userMap.get(userId);
            usersList.add(GameManager.getUserVo(user));
        }


        userOfRoom.setUserList(usersList);
        userOfRoom.setInRoomNumber(users.size());
        userOfRoom.setReadyNumber(readyNumber);


        player.sendMsg(new ResponseVo("roomService","joinRoom",new RoomTianDaKengVo(this,player)));

        Player.sendMsg2Player(new ResponseVo("roomService","roomNotice",userOfRoom), this.getUsers());


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


    public int quitRoom(Player player) {
        long userId = player.getUserId();
        if (!this.users.contains(userId)) {
            return ErrorCode.CANNOT_QUIT_ROOM_NOT_EXIST;

        }

        if (isInGame) {
            return ErrorCode.CANNOT_QUIT_ROOM_IS_IN_GAME;
        }

        roomRemoveUser(player);
        //删除玩家房间映射关系
        GameManager.getInstance().getUserRoom().remove(userId);

        if (this.createUser == player.getUserId()) {//房主解散

            //退钱
            User user = userMap.get(createUser);
            if (user != null) {
                user.setMoney(user.getMoney() + getNeedMoney(this.gameNumber));
            }
            Notice n = new Notice();
            n.setMessage("roomNum "+this.getRoomId()+" :has destroy success!");
            Player.sendMsg2Player(new ResponseVo("roomService","destroyRoom",n), this.getUsers());
            //删除房间
            GameManager.getInstance().roomsOfTanDaKeng.remove(roomId);
            GameManager.getInstance().getUsersSaveInDB().put(user.getUserId(),user);
        }

        noticeQuitRoom(player);

        return 0;
    }


    protected void noticeQuitRoom(Player player){
        List<UserVo> usersList = new ArrayList<>();
        UserOfRoom userOfRoom = new UserOfRoom();

        List<Long> noticeList = this.getUsers();

        for (long userId : users) {
            User user = this.userMap.get(userId);
            usersList.add(GameManager.getUserVo(user));
        }
        int inRoomNumber = this.getUsers().size();
        int readyNumber = 0;

        for (int i : this.getUserStatus().values()) {
            if(i==STATUS_READY){
                readyNumber++;
            }
        }
        userOfRoom.setUserList(usersList);
        userOfRoom.setInRoomNumber(inRoomNumber);
        userOfRoom.setReadyNumber(readyNumber);

        ResponseVo noticeResult = new ResponseVo("roomService", "roomNotice", userOfRoom);
        Player.sendMsg2Player(noticeResult, noticeList);

        Notice n = new Notice();
        n.setMessage("quit room success!");

        ResponseVo result = new ResponseVo("roomService","quitRoom",n);
        player.sendMsg(result);

    }

    public int getReady(Player player) {
        long userId = player.getUserId();
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
        Player.sendMsg2Player(new ResponseVo("roomService","noticeReady",noticeReady), this.users);

        //开始游戏
        if (readyNum >= personNumber) {
            startGame();
        }
        player.sendMsg(new ResponseVo("roomService","getReady",0));
        return 0;
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
    }


    private void startGame() {
        this.isInGame = true;
        GameTianDaKeng game = new GameTianDaKeng();

        //扣钱
        if (curGameNumber == 1) {
            spendMoney();
        }
        game.startGame(users);
        this.game = game;

        //通知其他人游戏已经开始
        CardEntity cardBegin = new CardEntity();
        cardBegin.setCurrentUserId(this.getBankerId() + "");
        Player.sendMsg2Player(new ResponseVo("gameService","gameBegin","ok"), this.getUsers());
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


    public int dissolution(Player player,boolean agreeOrNot,String methodName) {
        long userId = player.getUserId();
        if (!this.users.contains(userId)) {
            return ErrorCode.CANNOT_FIND_THIS_USER;

        }

        this.userStatus.put(userId, agreeOrNot ? STATUS_AGREE_DISSOLUTION : STATUS_DISSOLUTION);

        //第一个点解散
        if (agreeOrNot && !isHasDissolutionRequest) {
            isCanDissloution = true;
            this.isHasDissolutionRequest = true;
            //第一次申请 五分钟后解散
            long start = System.currentTimeMillis();
            TimerNode node = new TimerNode(start, FIVE_MIN, false, ()-> {

                if (isCanDissloution) {
                    dissolutionRoom();
                    logger.info("===2定时解散 roomId: "+roomId);
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
        Player.sendMsg2Player(new ResponseVo("roomService","noticeAnswerIfDissolveRoom",accept), this.users);


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
            GameTimer.getInstance().removeNode(timerNode);
            dissolutionRoom();
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


        AskQuitRoom accept1 = new AskQuitRoom();
        accept1.setUserId(""+userId);
        Player.sendMsg2Player("roomService","noticeDissolveRoom",accept1,users);

        AskQuitRoom send = new AskQuitRoom();
        send.setNote("ok");
        player.sendMsg("roomService",methodName,send);
        return 0;
    }

    private void dissolutionRoom(){



        GameManager.getInstance().rooms.remove(this.roomId);
        // 结果类
        ArrayList<UserOfResult> userOfResultList = new ArrayList<>();
        long time = System.currentTimeMillis();
        for(User user : this.userMap.values()){
            UserOfResult resultObj = new UserOfResult();
            try {
                resultObj.setUsername(URLDecoder.decode(user.getUsername(), "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            resultObj.setImage(user.getImage());
            resultObj.setScores(""+this.userScores.get(user.getUserId()));
            resultObj.setUserId(user.getUserId());
            resultObj.setTime(time);

            userOfResultList.add(resultObj);
            //删除映射关系
            GameManager.getInstance().getUserRoom().remove(user.getUserId());
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
        noticeEndResult.put("params", gameOfResult);
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
        GameManager.getInstance().getUsersSaveInDB().put(user.getUserId(),user);
    }

    public void spendMoney() {
        User user = userMap.get(this.createUser);
        if (user != null) {
            user.setMoney(user.getMoney() - createNeedMoney);
        }
        GameManager.getInstance().getUsersSaveInDB().put(user.getUserId(),user);
    }


    public String getRoomId() {
        return roomId;
    }

    public RoomTanDaKeng setRoomId(String roomId) {
        this.roomId = roomId;
        return this;
    }

    public int getCreateNeedMoney() {
        return createNeedMoney;
    }

    public RoomTanDaKeng setCreateNeedMoney(int createNeedMoney) {
        this.createNeedMoney = createNeedMoney;
        return this;
    }

    public Map<Long, Integer> getUserStatus() {
        return userStatus;
    }

    public RoomTanDaKeng setUserStatus(Map<Long, Integer> userStatus) {
        this.userStatus = userStatus;
        return this;
    }

    public List<Long> getUsers() {
        return users;
    }

    public RoomTanDaKeng setUsers(List<Long> users) {
        this.users = users;
        return this;
    }

    public Map<Long, Integer> getUserScores() {
        return userScores;
    }

    public RoomTanDaKeng setUserScores(Map<Long, Integer> userScores) {
        this.userScores = userScores;
        return this;
    }

    public Map<Long, User> getUserMap() {
        return userMap;
    }

    public RoomTanDaKeng setUserMap(Map<Long, User> userMap) {
        this.userMap = userMap;
        return this;
    }

    public double getRoomType() {
        return roomType;
    }

    public void setRoomType(double roomType) {
        this.roomType = roomType;
    }


    public int getGameNumber() {
        return gameNumber;
    }

    public RoomTanDaKeng setGameNumber(int gameNumber) {
        this.gameNumber = gameNumber;
        return this;
    }

    public int getCurGameNumber() {
        return curGameNumber;
    }

    public RoomTanDaKeng setCurGameNumber(int curGameNumber) {
        this.curGameNumber = curGameNumber;
        return this;
    }

    public int getPersonNumber() {
        return personNumber;
    }

    public RoomTanDaKeng setPersonNumber(int personNumber) {
        this.personNumber = personNumber;
        return this;
    }

    public long getCreateUser() {
        return createUser;
    }

    public RoomTanDaKeng setCreateUser(long createUser) {
        this.createUser = createUser;
        return this;
    }

    public int getBankerId() {
        return bankerId;
    }

    public RoomTanDaKeng setBankerId(int bankerId) {
        this.bankerId = bankerId;
        return this;
    }

    public boolean isInGame() {
        return isInGame;
    }

    public RoomTanDaKeng setInGame(boolean inGame) {
        isInGame = inGame;
        return this;
    }

    public boolean isHasDissolutionRequest() {
        return isHasDissolutionRequest;
    }

    public RoomTanDaKeng setHasDissolutionRequest(boolean hasDissolutionRequest) {
        isHasDissolutionRequest = hasDissolutionRequest;
        return this;
    }

    public TimerNode getTimerNode() {
        return timerNode;
    }

    public RoomTanDaKeng setTimerNode(TimerNode timerNode) {
        this.timerNode = timerNode;
        return this;
    }

    public GameTianDaKeng getGame() {
        return game;
    }

    public RoomTanDaKeng setGame(GameTianDaKeng game) {
        this.game = game;
        return this;
    }

    public String getGameType() {
        return gameType;
    }

    public RoomTanDaKeng setGameType(String gameType) {
        this.gameType = gameType;
        return this;
    }

    public boolean isCanDissloution() {
        return isCanDissloution;
    }

    public RoomTanDaKeng setCanDissloution(boolean canDissloution) {
        isCanDissloution = canDissloution;
        return this;
    }

}
