package com.code.server.cardgame.playdice;

import com.code.server.cardgame.core.Game;
import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.core.Room;
import com.code.server.cardgame.response.GameFinalResult;
import com.code.server.cardgame.response.ResponseVo;
import com.code.server.cardgame.utils.SpringUtil;
import com.code.server.db.Service.UserService;
import com.code.server.db.model.Record;
import com.code.server.db.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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

    private static final Integer MAX_BET_NUM = 9;

    private static final Logger logger = LoggerFactory.getLogger(GameDice.class);

    /*
        下注		    22全下注(用于庄)  21已下注   20未下注
        杀/不杀		41杀       40不杀
        摇筛子		50
        结果        61赢       60输
    */
    protected Map<Long,Integer> gameUserStatus = new HashMap<>();

    protected Map<Long,Double> gameUserScore = new HashMap<>();

    protected Map<Long,ThreePlayerScore> gameThreeScore = new HashMap<>();

    protected Map<Long,Double> gameResultScore = new HashMap<>();//结局分数

    protected Map<Long,PlayerCardInfoDice> playerCardInfos = new HashMap<>();

    protected Map<Long,List<Integer>> allDiceNumber = new HashMap<>();//所有玩家点数

    private List<Long> currentTurn = new ArrayList<>();//当前操作人

    protected boolean ifAgainBanker = true;

    protected Room room;//房间

    @Override
    public void startGame(List<Long> users,Room room){
        this.room = room;
        RoomDice roomDice = (RoomDice)room;
        if(roomDice.getCurBanker()==null){
            roomDice.setCurBanker(users.get(0));
            this.room.setBankerId(users.get(0));
            this.room.setLastDraw(true);//表示开局的标识
        }
        this.room.setMultiple(0);
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

            ThreePlayerScore threePlayerScore = new ThreePlayerScore();
            threePlayerScore.setUserId(uid);
            threePlayerScore.setPlayerCardInfoDice(playerCardInfo);
            threePlayerScore.setOne(0);
            threePlayerScore.setTwo(0);
            threePlayerScore.setThree(0);
            gameThreeScore.put(uid,threePlayerScore);
        }
        noticeWhoIsBanker();

        //断线专用
        List<Long> currentTurnList = new ArrayList<>();
        currentTurnList.addAll(users);
        currentTurnList.remove(room.getBankerId());
        currentTurn.addAll(currentTurnList);
    }


    /**
     * 下注
     * @param player
     * @return
     */
    public int bet(Player player,int chip,int chip2,int chip3){
        gameUserStatus.put(player.getUserId(),21);
        currentTurn.remove(player.getUserId());
        if(currentTurn.size()==0){
            currentTurn.add(room.getBankerId());
            gameUserStatus.put(room.getBankerId(),22);
        }

        logger.info(player.getUser().getAccount() +"  下注: "+ chip+","+chip2+","+chip3);

        if(chip > MAX_BET_NUM){//下注错误
            return ErrorCodeDice.MORE_BET;
        }
        RoomDice roomDice = (RoomDice)room;
        if(player.getUserId()==roomDice.getCurBanker()){
            return ErrorCodeDice.BANKER_NO_NEED_BET;//已经下注
        }

        if(gameUserScore.get(player.getUserId())==null){
            gameUserScore.put(player.getUserId(),chip+0.0);
        }else{
            return ErrorCodeDice.HAVE_BET;//已经下注
        }

        gameThreeScore.get(player.getUser().getUserId()).setOne(gameThreeScore.get(player.getUser().getUserId()).getOne()+chip);
        gameThreeScore.get(player.getUser().getUserId()).setTwo(gameThreeScore.get(player.getUser().getUserId()).getTwo()+chip2);
        gameThreeScore.get(player.getUser().getUserId()).setThree(gameThreeScore.get(player.getUser().getUserId()).getThree()+chip3);

        player.sendMsg(new ResponseVo("gameService","bet",0));
        noticeGameUserScore(player.getUserId());//通知所有人当前下注的情况

        if(gameUserScore.keySet().size()==this.room.getPersonNumber()-1){
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
        int temp = 0;
        for (Long l:gameUserStatus.keySet()) {
            if(41==gameUserStatus.get(l)){
                temp+=1;
            }
        }
        if(temp<0){
            return ErrorCodeDice.CANNOT_ROCK_NO_KILL;//已经下注
        }
        if(gameUserStatus.get(player.getUserId())>50){
            return ErrorCodeDice.CANNOT_ROCK_HAVE_ROCK;//已经下注
        }
        RoomDice roomDice = (RoomDice)room;
        List<Integer> list = DiceNumberUtils.getPoints();//获取点数
        room.setMultiple(DiceNumberUtils.getListTurnInt(list));
        noticeRockResult(player.getUserId(),list);
        if(player.getUserId()==roomDice.getCurBanker()){//判断是否是庄家
            if(!DiceNumberUtils.getIsEffective(list)){
                System.out.println("骰子点数为"+list+":重新摇骰子");
                noticeOtherCanRock(player.getUserId());
                player.sendMsg(new ResponseVo("gameService","rock",0));
                return 0;
            }
            else if(player.getUserId()==roomDice.getCurBanker() && DiceNumberUtils.getKill(list)){//庄营
                allDiceNumber.put(player.getUserId(),list);
                gameUserStatus.put(roomDice.getCurBanker(),61);
                for(Long l:room.getUsers()){
                    if(gameUserStatus.get(l)==41){
                        gameUserStatus.put(l,60);
                    }
                }
                noticeBankerWin();
                winAllEnd(list);
                noticeGG();
            }else if (player.getUserId()==roomDice.getCurBanker() && DiceNumberUtils.getCompensate(list)){//庄输
                allDiceNumber.put(player.getUserId(),list);
                gameUserStatus.put(roomDice.getCurBanker(),60);
                for(Long l:room.getUsers()){
                    if(gameUserStatus.get(l)==41){
                        gameUserStatus.put(l,61);
                    }
                }
                noticeBankerLost();
                loseAllEnd(list);
                ifAgainBanker = false;
                noticeGG();
            }else{//等待闲家摇
                gameUserStatus.put(player.getUserId(),50);
                allDiceNumber.put(player.getUserId(),list);
                Long userId = nextRockOne2(room.getUsers(),player.getUserId());
                noticeOtherCanRock(userId);
                currentTurn.remove(room.getBankerId());
                currentTurn.add(userId);
            }
        }else{
            if(!DiceNumberUtils.getIsEffective(list)){
                System.out.println("骰子点数为"+list+":重新摇骰子");
                noticeOtherCanRock(player.getUserId());
                player.sendMsg(new ResponseVo("gameService","rock",0));
                return 0;
            }
            allDiceNumber.put(player.getUserId(),list);
            Long winner = DiceNumberUtils.getMaxUser(this,roomDice.getCurBanker(),player.getUserId());
            noticeWhoWin(winner);
            if(winner==roomDice.getCurBanker()){//庄赢
//                gameResultScore.put(winner,gameResultScore.get(winner)+gameUserScore.get(player.getUserId()));
//                gameResultScore.put(player.getUserId(),gameResultScore.get(player.getUserId())-gameUserScore.get(player.getUserId()));
                //room.getUserScores().put(winner,room.getUserScores().get(winner)+gameUserScore.get(player.getUserId()));
                //room.getUserScores().put(player.getUserId(),room.getUserScores().get(player.getUserId())-gameUserScore.get(player.getUserId()));
                if(DiceNumberUtils.getPoint(list)==8){//2dao
                    gameResultScore.put(winner,gameResultScore.get(winner)+gameThreeScore.get(player.getUserId()).getOne());
                    gameResultScore.put(player.getUserId(),gameResultScore.get(player.getUserId())-gameThreeScore.get(player.getUserId()).getOne());
                    gameResultScore.put(winner,gameResultScore.get(winner)+gameThreeScore.get(player.getUserId()).getTwo());
                    gameResultScore.put(player.getUserId(),gameResultScore.get(player.getUserId())-gameThreeScore.get(player.getUserId()).getTwo());
                }else{//1dao
                    gameResultScore.put(winner,gameResultScore.get(winner)+gameThreeScore.get(player.getUserId()).getOne());
                    gameResultScore.put(player.getUserId(),gameResultScore.get(player.getUserId())-gameThreeScore.get(player.getUserId()).getOne());
                }

            }else{
//                gameResultScore.put(winner,gameResultScore.get(winner)+gameUserScore.get(winner));
//                gameResultScore.put(roomDice.getCurBanker(),gameResultScore.get(roomDice.getCurBanker())-gameUserScore.get(winner));
                //room.getUserScores().put(winner,room.getUserScores().get(winner)+gameUserScore.get(winner));
                //room.getUserScores().put(roomDice.getCurBanker(),room.getUserScores().get(roomDice.getCurBanker())-gameUserScore.get(winner));
                if(DiceNumberUtils.getPoint(list)==7){//3dao
                    gameResultScore.put(winner,gameResultScore.get(winner)+gameThreeScore.get(winner).getOne());
                    gameResultScore.put(roomDice.getCurBanker(),gameResultScore.get(roomDice.getCurBanker())-gameThreeScore.get(winner).getOne());
                    gameResultScore.put(winner,gameResultScore.get(winner)+gameThreeScore.get(winner).getTwo());
                    gameResultScore.put(roomDice.getCurBanker(),gameResultScore.get(roomDice.getCurBanker())-gameThreeScore.get(winner).getTwo());
                    gameResultScore.put(winner,gameResultScore.get(winner)+gameThreeScore.get(winner).getThree());
                    gameResultScore.put(roomDice.getCurBanker(),gameResultScore.get(roomDice.getCurBanker())-gameThreeScore.get(winner).getThree());
                }else if(DiceNumberUtils.getPoint(list)==9){//2dao
                    gameResultScore.put(winner,gameResultScore.get(winner)+gameThreeScore.get(winner).getOne());
                    gameResultScore.put(roomDice.getCurBanker(),gameResultScore.get(roomDice.getCurBanker())-gameThreeScore.get(winner).getOne());
                    gameResultScore.put(winner,gameResultScore.get(winner)+gameThreeScore.get(winner).getTwo());
                    gameResultScore.put(roomDice.getCurBanker(),gameResultScore.get(roomDice.getCurBanker())-gameThreeScore.get(winner).getTwo());
                }else{
                    gameResultScore.put(winner,gameResultScore.get(winner)+gameThreeScore.get(winner).getOne());
                    gameResultScore.put(roomDice.getCurBanker(),gameResultScore.get(roomDice.getCurBanker())-gameThreeScore.get(winner).getOne());
                }
                ifAgainBanker = false;
            }
            Long userId = nextRockOne2(room.getUsers(),player.getUserId());
            currentTurn.remove(player.getUserId());
            currentTurn.add(userId);
            if(userId!=0l){
                noticeOtherCanRock(userId);
            }else{
                noticeGG();
            }
        }
        player.sendMsg(new ResponseVo("gameService","rock",0));
        return 0;
    }

    /**
     * 杀
     * @param player
     * @return
     */
    public int kill(Player player,Long userId){
        gameUserStatus.put(userId,41);
        noticeWhoKilled(userId);
        ResponseVo vo = new ResponseVo("gameService","kill",0);
        Player.sendMsg2Player(vo,player.getUserId());
        return 0;
    }

    /**
     * 杀
     * @param player
     * @return
     */
    public int killAll(Player player){
        List<Long> userIdList = new ArrayList<>();
        for (Long l:room.getUsers()) {
            if(room.getBankerId()!=l){
                gameUserStatus.put(l,41);
                userIdList.add(l);
            }
        }
        noticeWhoKilledAll(userIdList);
        player.sendMsg(new ResponseVo("gameService","killAll",0));
        return 0;
    }

    /**
     * 通知开局庄家是谁
     */
    private void noticeWhoIsBanker(){
        RoomDice roomDice = (RoomDice)room;
        Map<String, Object> result = new HashMap<>();
        result.put("curBanker",roomDice.getCurBanker());
        ResponseVo vo = new ResponseVo("gameService","noticeWhoIsBanker",result);
        Player.sendMsg2Player(vo,room.getUsers());
    }

    /**
     * 通知通知所有人下的注数
     */
    private void noticeGameUserScore(Long userId){
        Map<String, Object> result = new HashMap<>();
        //result.put("gameUserScore",gameUserScore);
        result.put("gameThreeScore",gameThreeScore);
        result.put("userId",userId);
        ResponseVo vo = new ResponseVo("gameService","noticeGameUserScorer",result);
        Player.sendMsg2Player(vo,room.getUsers());
    }

    /**
     * 通知通知所有人下注完毕,庄家可以摇色子
     */
    private void noticeBankerCanKill(){
        RoomDice roomDice = (RoomDice)room;
        Map<String, Object> result = new HashMap<>();
        result.put("curBanker",roomDice.getCurBanker());
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
     * 通知通知所有人下注完毕
     */
    private void noticeRockResult(Long userId,List<Integer> rockResult){
        Map<String, Object> result = new HashMap<>();
        result.put("rockUserId",userId);
        result.put("rockResult",rockResult);
        result.put("point",DiceNumberUtils.getPoint(rockResult));
        ResponseVo vo = new ResponseVo("gameService","noticeRockResult",result);
        Player.sendMsg2Player(vo,room.getUsers());
    }

    /**
     * 通知再摇一次
     */
/*    private void noticeRockAgain(Long againUserId){
        Map<String, Object> result = new HashMap<>();
        result.put("againUserId",againUserId);
        ResponseVo vo = new ResponseVo("gameService","noticeRockAgain",result);
        Player.sendMsg2Player(vo,room.getUsers());
    }*/

    /**
     * 通知通知谁被杀
     */
    private void noticeWhoKilled(Long userId){
        Map<String, Object> result = new HashMap<>();
        result.put("killedUserId",userId);
        result.put("gameUserStatus",gameUserStatus);
        ResponseVo vo = new ResponseVo("gameService","noticeWhoKilled",result);
        Player.sendMsg2Player(vo,room.getUsers());
    }

    /**
     * 通知通知谁被杀
     */
    private void noticeWhoKilledAll(List<Long> userIdList){
        Map<String, Object> result = new HashMap<>();
        result.put("userIdList",userIdList);
        result.put("gameUserStatus",gameUserStatus);
        ResponseVo vo = new ResponseVo("gameService","noticeWhoKilledAll",result);
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
        RoomDice roomDice = (RoomDice)room;
        Map<String, Object> result = new HashMap<>();
        result.put("allDiceNumber",allDiceNumber);
        result.put("curBanker",roomDice.getCurBanker());
        ResponseVo vo = new ResponseVo("gameService","noticeBankerWin",result);
        Player.sendMsg2Player(vo,room.getUsers());
    }

    /**
     * 通知庄输
     */
    private void noticeBankerLost(){
        RoomDice roomDice = (RoomDice)room;
        Map<String, Object> result = new HashMap<>();
        result.put("allDiceNumber",allDiceNumber);
        result.put("curBanker",roomDice.getCurBanker());
        ResponseVo vo = new ResponseVo("gameService","noticeBankerLost",result);
        Player.sendMsg2Player(vo,room.getUsers());
    }

    /**
     * 通知此局结束
     */
    private void noticeGG(){
        RoomDice roomDice = (RoomDice)room;
        if(!ifAgainBanker){
            long bankerId = nextOne(room.getUsers(),room.getBankerId());
            this.room.setBankerId(bankerId);
            roomDice.setCurBanker(bankerId);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("allDiceNumber",allDiceNumber);
        result.put("gameResultScore",gameResultScore);
        result.put("curBanker",roomDice.getCurBanker());
        ResponseVo vo = new ResponseVo("gameService","noticeGG",result);
        Player.sendMsg2Player(vo,room.getUsers());

        genRecord();
        room.clearReadyStatus(true);
        sendFinalResult();
    }


    //=================================================================================

    public void winAllEnd(List<Integer> list){
        RoomDice roomDice = (RoomDice)room;
        long bankerId = roomDice.getCurBanker();
        int temp = 0;
        /*for (Long l:gameUserScore.keySet()) {
            if(l!=roomDice.getCurBanker() && gameUserStatus.get(l)==60){
                temp+=gameUserScore.get(l);
                gameResultScore.put(l,gameResultScore.get(l)-gameUserScore.get(l));
                //room.getUserScores().put(l,gameResultScore.get(l)-gameUserScore.get(l));
            }
        }
        gameResultScore.put(bankerId,gameResultScore.get(bankerId)+temp);*/
        //room.getUserScores().put(bankerId,gameResultScore.get(bankerId)+temp);
        if(DiceNumberUtils.getPoint(list)==6){//1dao
            for (Long l:gameThreeScore.keySet()) {
                if(l!=roomDice.getCurBanker() && gameUserStatus.get(l)==60){
                    temp+=gameThreeScore.get(l).getOne();
                    gameResultScore.put(l,gameResultScore.get(l)-gameThreeScore.get(l).getOne());
                }
            }
            gameResultScore.put(bankerId,gameResultScore.get(bankerId)+temp);
        }else if(DiceNumberUtils.getPoint(list)==9){//2dao
            for (Long l:gameThreeScore.keySet()) {
                if(l!=roomDice.getCurBanker() && gameUserStatus.get(l)==60){
                    temp+=gameThreeScore.get(l).getOne();
                    gameResultScore.put(l,gameResultScore.get(l)-gameThreeScore.get(l).getOne());
                }
            }
            for (Long l:gameThreeScore.keySet()) {
                if(l!=roomDice.getCurBanker() && gameUserStatus.get(l)==60){
                    temp+=gameThreeScore.get(l).getTwo();
                    gameResultScore.put(l,gameResultScore.get(l)-gameThreeScore.get(l).getTwo());
                }
            }
            gameResultScore.put(bankerId,gameResultScore.get(bankerId)+temp);
        }else if(DiceNumberUtils.getPoint(list)==7){//3dao
            for (Long l:gameThreeScore.keySet()) {
                if(l!=roomDice.getCurBanker() && gameUserStatus.get(l)==60){
                    temp+=gameThreeScore.get(l).getOne();
                    gameResultScore.put(l,gameResultScore.get(l)-gameThreeScore.get(l).getOne());
                }
            }
            for (Long l:gameThreeScore.keySet()) {
                if(l!=roomDice.getCurBanker() && gameUserStatus.get(l)==60){
                    temp+=gameThreeScore.get(l).getTwo();
                    gameResultScore.put(l,gameResultScore.get(l)-gameThreeScore.get(l).getTwo());
                }
            }
            for (Long l:gameThreeScore.keySet()) {
                if(l!=roomDice.getCurBanker() && gameUserStatus.get(l)==60){
                    temp+=gameThreeScore.get(l).getThree();
                    gameResultScore.put(l,gameResultScore.get(l)-gameThreeScore.get(l).getThree());
                }
            }
            gameResultScore.put(bankerId,gameResultScore.get(bankerId)+temp);
        }
    }

    public void loseAllEnd(List<Integer> list){
        RoomDice roomDice = (RoomDice)room;
        long bankerId = roomDice.getCurBanker();
        int temp = 0;
/*        for (Long l:gameUserScore.keySet()) {
            if(l!=roomDice.getCurBanker() && gameUserStatus.get(l)==61){
                temp+=gameUserScore.get(l);
                gameResultScore.put(l,gameResultScore.get(l)+gameUserScore.get(l));
                //room.getUserScores().put(l,gameResultScore.get(l)+gameUserScore.get(l));
            }
        }
        gameResultScore.put(bankerId,gameResultScore.get(bankerId)-temp);*/
        //room.getUserScores().put(bankerId,gameResultScore.get(bankerId)-temp);
        //roomDice.setCurBanker(nextOne(room.getUsers(),bankerId));
        if(DiceNumberUtils.getPoint(list)==1){
            for (Long l:gameThreeScore.keySet()) {
                if(l!=roomDice.getCurBanker() && gameUserStatus.get(l)==61){
                    temp+=gameThreeScore.get(l).getOne();
                    gameResultScore.put(l,gameResultScore.get(l)+gameThreeScore.get(l).getOne());
                }
            }
        }else if(DiceNumberUtils.getPoint(list)==8){
            for (Long l:gameThreeScore.keySet()) {
                if(l!=roomDice.getCurBanker() && gameUserStatus.get(l)==61){
                    temp+=gameThreeScore.get(l).getOne();
                    gameResultScore.put(l,gameResultScore.get(l)+gameThreeScore.get(l).getOne());
                }
            }
            for (Long l:gameThreeScore.keySet()) {
                if(l!=roomDice.getCurBanker() && gameUserStatus.get(l)==61){
                    temp+=gameThreeScore.get(l).getTwo();
                    gameResultScore.put(l,gameResultScore.get(l)+gameThreeScore.get(l).getTwo());
                }
            }
        }
        gameResultScore.put(bankerId,gameResultScore.get(bankerId)-temp);
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
        RoomDice roomDice = (RoomDice)room;
        for (Long l:gameResultScore.keySet()) {
            room.getUserScores().put(l,room.getUserScores().get(l)+gameResultScore.get(l));
        }
        if (roomDice.getCurCricleNumber() > roomDice.getCricle()) {
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
        RoomDice roomDice = (RoomDice)room;
        int index = list.indexOf(curId);

        int nextId = index + 1;
        if (nextId >= list.size()) {
            nextId = 0;
            roomDice.setCurCricleNumber(roomDice.getCurCricleNumber()+1);
        }
        return list.get(nextId);
    }

    /**
     * 下个人摇色子
     * @param curId
     * @return
     */
    @Deprecated
    public long nextRockOne(List<Long> list, long curId) {
        Long result = 0l;
        int index = list.indexOf(curId);

        int nextId = index + 1;
        if (nextId >= list.size()) {
            return 0l;
        }
        result = list.get(nextId);
        if(gameUserStatus.get(result)==21){
            nextRockOne(list,result);
        }else if(gameUserStatus.get(result)==41){
            return result;
        }else{
            return result;
        }
        return result;
    }


    /**
     * 下个人摇色子
     * @param curId
     * @return
     */
    public long nextRockOne2(List<Long> list, long curId) {
        Long result = 0l;
        List<Long> beforeList = new ArrayList<>();
        List<Long> afterList = new ArrayList<>();
        List<Long> lastList = new ArrayList<>();

        int indexBanker = list.indexOf(room.getBankerId());
        for (Long l:list) {
            if(list.indexOf(l)<indexBanker){
                afterList.add(l);
            }else if(list.indexOf(l)>indexBanker){
                beforeList.add(l);
            }else if(list.indexOf(l)==indexBanker){
                lastList.add(l);
            }
        }
        lastList.addAll(beforeList);
        lastList.addAll(afterList);
        int position = lastList.indexOf(curId);
        for (Long l:lastList) {
            if(lastList.indexOf(l)>position && gameUserStatus.get(l)==41){
                result = l;
                return result;
            }
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

    public List<Long> getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(List<Long> currentTurn) {
        this.currentTurn = currentTurn;
    }

    public Map<Long, ThreePlayerScore> getGameThreeScore() {
        return gameThreeScore;
    }

    public void setGameThreeScore(Map<Long, ThreePlayerScore> gameThreeScore) {
        this.gameThreeScore = gameThreeScore;
    }
}
