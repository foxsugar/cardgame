package com.code.server.cardgame.playdice;

import com.code.server.cardgame.core.Game;
import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.response.GameFinalResult;
import com.code.server.cardgame.response.ResponseVo;
import com.code.server.cardgame.utils.SpringUtil;
import com.code.server.db.Service.UserService;
import com.code.server.db.model.Record;
import com.code.server.db.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
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
public class GameDice extends Game {

    private static final Integer MAX_BET_NUM = 5;

    private static final Logger logger = LoggerFactory.getLogger(GameDice.class);

    /*
        下注		    21已下注   20未下注
        杀/不杀		41杀       40不杀
        摇筛子		50
        结果        61赢       60输
    */
    protected Map<Long,Integer> gameUserStatus = new HashMap<>();

    protected Map<Long,Double> gameUserScore = new HashMap<>();

    protected Map<Long,Double> gameResultScore = new HashMap<>();//结局分数

    protected Map<Long,PlayerCardInfoDice> playerCardInfos = new HashMap<>();

    protected Map<Long,List<Integer>> allDiceNumber = new HashMap<>();//所有玩家点数

    private long currentTurn;//当前操作人

    protected RoomDice room;//房间

    public void startGame(List<Long> users,RoomDice room){
        this.room = room;
        if(room.getCurBanker()==null){
            room.setCurBanker(users.get(0));
        }
        init(users);
    }
    public void init(List<Long> users){
        //初始化玩家
        for(Long uid : users){
            PlayerCardInfoDice playerCardInfo = new PlayerCardInfoDice();
            playerCardInfo.userId = uid;
            playerCardInfos.put(uid,playerCardInfo);
            gameUserStatus.put(uid,20);
            gameResultScore.put(uid,0.0);
        }
        noticeWhoIsBanker();
    }


    /**
     * 下注
     * @param player
     * @return
     */
    public int bet(Player player,int chip){
        gameUserStatus.put(player.getUserId(),21);

        logger.info(player.getUser().getAccount() +"  下注: "+ chip);

        if(chip > MAX_BET_NUM){//下注错误
            return ErrorCodeDice.MORE_BET;
        }

        if(player.getUserId()==room.getCurBanker()){
            return ErrorCodeDice.BANKER_NO_NEED_BET;//已经下注
        }

        if(gameUserScore.get(player.getUserId())==null){
            gameUserScore.put(player.getUserId(),chip+0.0);
        }else{
            return ErrorCodeDice.HAVE_BET;//已经下注
        }

        player.sendMsg(new ResponseVo("gameService","bet",0));
        noticeGameUserScore();//通知所有人当前下注的情况

        if(gameUserScore.keySet().size()==4){
            noticeBankerCanKill();
        }

        return 0;
    }

    /**
     * 摇色子
     * @param player
     * @return
     */
    public int rock(Player player){
        gameUserStatus.put(player.getUserId(),50);
        List<Integer> list = DiceNumberUtils.getPoints();//获取点数
        allDiceNumber.put(player.getUserId(),list);
        if(player.getUserId()==room.getCurBanker()){//判断是否是庄家
            if(player.getUserId()==room.getCurBanker() && DiceNumberUtils.getKill(list)){//庄营
                gameUserStatus.put(room.getCurBanker(),61);
                for(Long l:room.getUsers()){
                    if(gameUserStatus.get(l)==41){
                        gameUserStatus.put(l,60);
                    }
                }
                noticeBankerWin();
                winAllEnd();
                noticeGG();
            }else if (player.getUserId()==room.getCurBanker() && DiceNumberUtils.getCompensate(list)){//庄输
                gameUserStatus.put(room.getCurBanker(),60);
                for(Long l:room.getUsers()){
                    if(gameUserStatus.get(l)==41){
                        gameUserStatus.put(l,61);
                    }
                }
                noticeBankerLost();
                loseAllEnd();
                noticeGG();
            }else if(!DiceNumberUtils.getIsEffective(list)){
                noticeRockAgain(player.getUserId());
            }else{//等待闲家摇
                Long userId = nextRockOne(room.getUsers(),player.getUserId());
                noticeOtherCanRock(userId);
            }
        }else{
            if(!DiceNumberUtils.getIsEffective(list)){
                noticeRockAgain(player.getUserId());
                return 0;
            }
            Long winner = DiceNumberUtils.getMaxUser(this,room.getCurBanker(),player.getUserId());
            noticeWhoWin(winner);
            if(winner==room.getCurBanker()){//庄赢
                room.getUserScores().put(winner,room.getUserScores().get(winner)+gameUserScore.get(player.getUserId()));
                room.getUserScores().put(player.getUserId(),room.getUserScores().get(player.getUserId())-gameUserScore.get(player.getUserId()));
            }else{
                room.getUserScores().put(winner,room.getUserScores().get(winner)+gameUserScore.get(player.getUserId()));
                room.getUserScores().put(room.getCurBanker(),room.getUserScores().get(room.getCurBanker())-gameUserScore.get(player.getUserId()));
            }
            Long userId = nextRockOne(room.getUsers(),player.getUserId());
            if(userId!=0l){
                noticeOtherCanRock(userId);
            }else{
                sendFinalResult();
                noticeGG();
            }
        }
        return 0;
    }

    /**
     * 杀
     * @param player
     * @return
     */
    public int kill(Player player,Long userId){
        gameUserStatus.put(userId,41);
        return 0;
    }



    /**
     * 通知开局庄家是谁
     */
    private void noticeWhoIsBanker(){
        Map<String, Object> result = new HashMap<>();
        result.put("curBanker",room.getCurBanker());
        ResponseVo vo = new ResponseVo("gameService","noticeWhoIsBanker",result);
        Player.sendMsg2Player(vo,room.getUsers());
    }

    /**
     * 通知通知所有人下的注数
     */
    private void noticeGameUserScore(){
        Map<String, Object> result = new HashMap<>();
        result.put("gameUserScore",gameUserScore);
        ResponseVo vo = new ResponseVo("gameService","noticeGameUserScorer",result);
        Player.sendMsg2Player(vo,room.getUsers());
    }

    /**
     * 通知通知所有人下注完毕,庄家可以摇色子
     */
    private void noticeBankerCanKill(){
        Map<String, Object> result = new HashMap<>();
        result.put("curBanker",room.getCurBanker());
        ResponseVo vo = new ResponseVo("gameService","noticeBankerCanKill",result);
        Player.sendMsg2Player(vo,room.getUsers());
    }

    /**
     * 通知通知所有人下注完毕
     */
    private void noticeOtherCanRock(Long userId){
        Map<String, Object> result = new HashMap<>();
        result.put("nextPlayer",userId);
        result.put("allDiceNumber",allDiceNumber);
        ResponseVo vo = new ResponseVo("gameService","noticeOtherCanRock",result);
        Player.sendMsg2Player(vo,room.getUsers());
    }
    /**
     * 通知再摇一次
     */
    private void noticeRockAgain(Long againUserId){
        Map<String, Object> result = new HashMap<>();
        result.put("againUserId",againUserId);
        ResponseVo vo = new ResponseVo("gameService","noticeOtherCanRock",result);
        Player.sendMsg2Player(vo,room.getUsers());
    }


    /**
     * 通知通知谁赢
     */
    private void noticeWhoWin(Long userId){
        Map<String, Object> result = new HashMap<>();
        result.put("winner",userId);
        result.put("allDiceNumber",allDiceNumber);
        ResponseVo vo = new ResponseVo("gameService","noticeWhoWin",result);
        Player.sendMsg2Player(vo,room.getUsers());
    }

    /**
     * 通知庄赢
     */
    private void noticeBankerWin(){
        Map<String, Object> result = new HashMap<>();
        result.put("allDiceNumber",allDiceNumber);
        result.put("curBanker",room.getCurBanker());
        ResponseVo vo = new ResponseVo("gameService","noticeBankerWin",result);
        Player.sendMsg2Player(vo,room.getUsers());
    }

    /**
     * 通知庄输
     */
    private void noticeBankerLost(){
        Map<String, Object> result = new HashMap<>();
        result.put("allDiceNumber",allDiceNumber);
        result.put("curBanker",room.getCurBanker());
        ResponseVo vo = new ResponseVo("gameService","noticeBankerLost",result);
        Player.sendMsg2Player(vo,room.getUsers());
    }

    /**
     * 通知此局结束
     */
    private void noticeGG(){
        Map<String, Object> result = new HashMap<>();
        result.put("allDiceNumber",allDiceNumber);
        result.put("curBanker",room.getCurBanker());
        ResponseVo vo = new ResponseVo("gameService","noticeGG",result);
        Player.sendMsg2Player(vo,room.getUsers());
    }


    //=================================================================================

    public void winAllEnd(){
        long bankerId = room.getCurBanker();
        int temp = 0;
        for (Long l:gameUserScore.keySet()) {
            if(l!=room.getCurBanker()){
                temp+=gameUserScore.get(l);
                gameResultScore.put(l,gameResultScore.get(l)-gameUserScore.get(l));
            }
        }
        gameResultScore.put(bankerId,gameResultScore.get(bankerId)+temp);
        genRecord();
        room.clearReadyStatus(true);
    }

    public void loseAllEnd(){
        long bankerId = room.getCurBanker();
        int temp = 0;
        for (Long l:gameUserScore.keySet()) {
            if(l!=room.getCurBanker()){
                temp+=gameUserScore.get(l);
                gameResultScore.put(l,gameResultScore.get(l)+gameUserScore.get(l));
            }
        }
        gameResultScore.put(bankerId,gameResultScore.get(bankerId)-temp);
        room.setCurBanker(nextOne(room.getUsers(),bankerId));
        genRecord();
        room.clearReadyStatus(true);
    }


    /**
     * 存入数据库
     */
    private void genRecord(){
        UserService userService = SpringUtil.getBean(UserService.class);
        Record.RoomRecord roomRecord = new Record.RoomRecord();
        roomRecord.setTime(System.currentTimeMillis());
        roomRecord.setType(room.getCreateType());
        for (long userId : room.getUsers()) {
            PlayerCardInfoDice playerCardInfo = playerCardInfos.get(userId);
            User user = room.getUserMap().get(userId);
            Record.UserRecord userRecord = new Record.UserRecord();
            userRecord.setName(user.getUsername());
            userRecord.setScore(room.getUserScores().get(userId));
            userRecord.setUserId(userId);
            userRecord.setRoomId(room.getRoomId());

            userService.save(user);
            roomRecord.addRecord(userRecord);
        }
        room.getUserMap().forEach((k,v)->v.getRecord().addRoomRecord(roomRecord));

        //加入数据库保存列表
        GameManager.getInstance().getSaveUser2DB().addAll(room.getUserMap().values());
    }

    private void sendFinalResult() {

        for (Long l:gameResultScore.keySet()) {
            room.getUserScores().put(l,room.getUserScores().get(l)+gameResultScore.get(l));
        }
        if (room.getCurCricleNumber() > room.getCricle()) {
            GameFinalResult gameFinalResult = new GameFinalResult();
            gameFinalResult.setEndTime(new Date().toLocaleString());
            room.getUserScores().forEach((userId,score)->{

                        gameFinalResult.getUserInfos().add(new GameFinalResult.UserInfo(userId,score));

                        //删除玩家房间映射关系
                        GameManager.getInstance().getUserRoom().remove(userId);
                    }
            );
            Player.sendMsg2Player("gameService","gameFinalResult",gameFinalResult,room.getUsers());

            Record.RoomRecord roomRecord = new Record.RoomRecord();
            roomRecord.setTime(System.currentTimeMillis());
            roomRecord.setType(room.getCreateType());
            for (long userId : room.getUsers()) {
                PlayerCardInfoDice playerCardInfo = playerCardInfos.get(userId);
                User user1 = room.getUserMap().get(userId);
                Record.UserRecord userRecord = new Record.UserRecord();
                userRecord.setName(user1.getUsername());
                userRecord.setScore(room.getUserScores().get(userId));
                userRecord.setUserId(userId);
                userRecord.setRoomId(room.getRoomId());
                roomRecord.addRecord(userRecord);
            }
            room.getUserMap().forEach((k,v)->v.getRecord().addRoomRecord(roomRecord));

            //加入数据库保存列表
            GameManager.getInstance().getSaveUser2DB().addAll(room.getUserMap().values());

            //删除room
            GameManager.getInstance().removeRoom(room);
        }
    }


    /**
     * 下个人
     * @param curId
     * @return
     */
    public long nextOne(List<Long> list, long curId) {
        int index = list.indexOf(curId);

        int nextId = index + 1;
        if (nextId >= list.size()) {
            nextId = 0;
            room.setCurCricleNumber(room.getCurCricleNumber()+1);
        }
        return list.get(nextId);
    }

    /**
     * 下个人摇色子
     * @param curId
     * @return
     */
    public long nextRockOne(List<Long> list, long curId) {
        Long result = 0l;
        int index = list.indexOf(curId);

        int nextId = index + 1;
        if (nextId >= list.size()) {
            nextId = 0;
        }
        result = list.get(nextId);
        if(gameUserStatus.get(result)==40){
            nextRockOne(list,result);
        }else if(gameUserStatus.get(result)==41){
            return result;
        }else{
            return result;
        }
        return result;
    }

    public Map<Long, Integer> getGameUserStatus() {
        return gameUserStatus;
    }

    public void setGameUserStatus(Map<Long, Integer> gameUserStatus) {
        this.gameUserStatus = gameUserStatus;
    }

    public Map<Long, Double> getGameUserScore() {
        return gameUserScore;
    }

    public void setGameUserScore(Map<Long, Double> gameUserScore) {
        this.gameUserScore = gameUserScore;
    }

    public Map<Long, Double> getGameResultScore() {
        return gameResultScore;
    }

    public void setGameResultScore(Map<Long, Double> gameResultScore) {
        this.gameResultScore = gameResultScore;
    }

    public Map<Long, PlayerCardInfoDice> getPlayerCardInfos() {
        return playerCardInfos;
    }

    public void setPlayerCardInfos(Map<Long, PlayerCardInfoDice> playerCardInfos) {
        this.playerCardInfos = playerCardInfos;
    }

    public Map<Long, List<Integer>> getAllDiceNumber() {
        return allDiceNumber;
    }

    public void setAllDiceNumber(Map<Long, List<Integer>> allDiceNumber) {
        this.allDiceNumber = allDiceNumber;
    }
}
