package com.code.server.cardgame.core.game;

import com.code.server.cardgame.core.*;
import com.code.server.cardgame.core.room.Room;
import com.code.server.cardgame.core.room.RoomTanDaKeng;
import com.code.server.cardgame.response.GameFinalResult;
import com.code.server.cardgame.response.ResponseVo;
import com.code.server.db.model.Record;
import com.code.server.db.model.User;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.code.server.cardgame.response.ErrorCodeTDK;

import java.util.*;

import static com.code.server.cardgame.core.CardUtil.getCardType;

/**
 * Created by sunxianping on 2017/3/13.
 */
public class GameTianDaKeng extends Game{
    private static final Logger logger = LoggerFactory.getLogger(GameTianDaKeng.class);

    private static final int INIT_BOTTOM_CHIP = 1;//底注
    private static final int INIT_CARD_NUM = 3;
    private static final int MAX_BET_NUM = 5;
    private static final int DOUBLE_MAX_BET_NUM = 10;//烂锅下一局的最大注上限

    protected List<Integer> cards = new ArrayList<>();//牌

    protected List<Integer> tableCards = new ArrayList<>();//剩余牌
    protected Map<Long,PlayerCardInfoTianDaKeng> playerCardInfos = new HashMap<>();
    protected List<Long> users = new ArrayList<>();
    private Random rand = new Random();

    protected Map<Long,Integer> allChip = new HashedMap();//总下注数
    protected Map<Long,Integer> curChip = new HashedMap();//当前下注数


    private long currentTurn;//当前操作人
    private int chip;//下注
    private int trunNumber;//第几张牌了


    protected List<Long> aliveUser = new ArrayList<>();//存活的人
    protected List<Long> curUser = new ArrayList<>();//本轮的人
    protected List<Long> canRaiseUser = new ArrayList<>();//可以反踢的人

    protected RoomTanDaKeng room;//房间

    public void startGame(List<Long> users,RoomTanDaKeng room){
        this.room = room;
        init(users);
    }
    public void init(List<Long> users){

        //初始化玩家
        for(Long uid : users){
            PlayerCardInfoTianDaKeng playerCardInfo = new PlayerCardInfoTianDaKeng();
            playerCardInfo.userId = uid;
            playerCardInfos.put(uid,playerCardInfo);
        }
        this.users.addAll(users);
        this.aliveUser.addAll(users);
        this.curUser.addAll(users);
        this.canRaiseUser.addAll(users);
        this.trunNumber = 1;

        shuffle();
        deal();
        if(!this.room.isLastDraw()){
            mustBet();
        }
        currentTurn = getMaxCardUser(trunNumber);
        noticeCanBet(getMaxCardUser(trunNumber));
    }



    /**
     * 洗牌
     */
    protected void shuffle(){
        for (int i = 37; i < 53; i+=4) {
            cards.add(i);
        }
        cards.add(1);
        cards.add(2);
        cards.add(3);
        cards.add(4);
        Collections.shuffle(cards);
    }

    /**
     * 发牌
     */
    public void deal(){
        for(PlayerCardInfoTianDaKeng playerCardInfo : playerCardInfos.values()){
            for(int i=0;i<INIT_CARD_NUM;i++){
                if(playerCardInfo.myselfCards.size()<2){
                    playerCardInfo.myselfCards.add(cards.remove(0));
                }else if(playerCardInfo.myselfCards.size()==2){
                    playerCardInfo.everyknowCards.add(cards.remove(0));
                }else{
                    break;
                }
            }
            playerCardInfo.allCards.addAll(playerCardInfo.myselfCards);
            playerCardInfo.allCards.addAll(playerCardInfo.everyknowCards);
            //通知自己发的2张底牌
            Player.sendMsg2Player(new ResponseVo("gameService","dealmyself",playerCardInfo.myselfCards),playerCardInfo.userId);
            //通知其他人的第三张明牌
            Player.sendMsg2Player(new ResponseVo("gameService","dealevery",playerCardInfo.everyknowCards),users);
        }

        //底牌
        tableCards.addAll(cards);
    }

    /**
     * 必须下底注
     */
    private void mustBet(){
        for (Long user : users) {
            allChip.put(user,INIT_BOTTOM_CHIP);
        }
        //通知下底注
        Player.sendMsg2Player(new ResponseVo("gameService","mustBet",allChip),users);

    }

    /**
     * 获取第一个叫牌的人
     * @param number    比较第几张牌，第一次为第一张
     * @return
     */
    public Long getMaxCardUser(int number){
        Long userId = null;
        int temp = 0;
        for (PlayerCardInfoTianDaKeng playerCardInfoTianDaKeng :playerCardInfos.values()) {
            if(temp < CardUtilOfTangDaKeng.getCardForScore().get(playerCardInfoTianDaKeng.everyknowCards.get(number-1))){
                temp = CardUtilOfTangDaKeng.getCardForScore().get(playerCardInfoTianDaKeng.everyknowCards.get(number-1));
                userId = playerCardInfoTianDaKeng.userId;
            }
        }
        return userId;
    }

    /**
     * 下注
     * @param player
     * @return
     */
    public int bet(Player player,int chip){

        logger.info(player.getUser().getAccount() +"  下注: "+ chip);
        if (currentTurn != player.getUserId()) {
            return ErrorCodeTDK.CANNOT_BET;
        }

        //TODO 烂锅之后需要重新判断
        if(chip > MAX_BET_NUM  || (this.room.isLastDraw() && chip>DOUBLE_MAX_BET_NUM)){//下注错误
            return ErrorCodeTDK.MORE_BET;
        }
        this.chip = chip;
        addToChip(player.getUserId(),chip);//添加积分
        curUser.remove(currentTurn);//本轮操作完删除
        currentTurn = nextTurnId(currentTurn);//下一个人
        noticeCanCall(currentTurn);//通知下一个人可以下注

        player.sendMsg(new ResponseVo("gameService","bet",0));
        return 0;
    }

    /**
     * 跟注
     * @param player
     * @return
     */
    public int call(Player player){
        logger.info(player.getUser().getAccount() +"  跟注: "+ chip);
        if (currentTurn != player.getUserId()) {
            return ErrorCodeTDK.CANNOT_BET;
        }

        //TODO 烂锅之后需要重新判断
        if(chip > MAX_BET_NUM || (this.room.isLastDraw() && chip>DOUBLE_MAX_BET_NUM)){//下注错误
            return ErrorCodeTDK.MORE_BET;
        }
        addToChip(player.getUserId(),chip);//添加积分
        curUser.remove(currentTurn);//本轮操作完删除

        branch();

        player.sendMsg(new ResponseVo("gameService","call",0));
        return 0;
    }

    /**
     * 加注，踢
     * @param player
     * @return
     */
    public int raise(Player player){
        logger.info(player.getUser().getAccount() +"  踢: "+ chip);
        if (currentTurn != player.getUserId()) {
            return ErrorCodeTDK.CANNOT_BET;
        }

        //TODO 烂锅之后需要重新判断
        if(chip > MAX_BET_NUM || (this.room.isLastDraw() && chip>DOUBLE_MAX_BET_NUM)){//下注错误
            return ErrorCodeTDK.MORE_BET;
        }
        this.chip = chip;
        addToChip(player.getUserId(),chip);//添加积分
        curUser.remove(currentTurn);//本轮操作完删除
        //canRaiseUser.remove(currentTurn);//每个人可以反踢一次，踢完删除
        currentTurn = nextTurnId(currentTurn);//下一个人
        noticeCanCall(currentTurn);//通知下一个人可以下注

        player.sendMsg(new ResponseVo("gameService","raise",0));
        return 0;
    }

    /**
     * 不跟,不踢
     * @param player
     * @return
     */
    public int pass(Player player){
        logger.info(player.getUser().getAccount() +"  不踢 ");

        curUser.remove(currentTurn);//本轮操作完删除

        branch();

        player.sendMsg(new ResponseVo("gameService","pass",0));
        return 0;
    }

    /**
     * 弃牌
     * @param player
     * @return
     */
    public int fold(Player player){
        logger.info(player.getUser().getAccount() +"  弃牌 ");

        noticeOtherFold(currentTurn);//通知其他人弃牌
        curUser.remove(currentTurn);//本轮操作完删除
        aliveUser.remove(currentTurn);//在玩的人中删除弃牌的
        canRaiseUser.remove(currentTurn);//在玩的人中删除弃牌的

        branch();

        player.sendMsg(new ResponseVo("gameService","fold",0));
        return 0;
    }

    /**
     * 发牌
     * @return
     */
    public void dealACard(){
        int temp = 0;//存公章
        for(PlayerCardInfoTianDaKeng playerCardInfo : playerCardInfos.values()){
           if(aliveUser.contains(playerCardInfo.userId)){//存活的人发牌
               if(tableCards.size() > 1){
                   playerCardInfo.everyknowCards.add(tableCards.remove(0));
                   playerCardInfo.allCards.add(playerCardInfo.everyknowCards.get(playerCardInfo.everyknowCards.size()-1));
               }else if(tableCards.size() == 1){
                   temp = tableCards.get(0);
                   playerCardInfo.everyknowCards.add(tableCards.remove(0));
                   playerCardInfo.allCards.add(playerCardInfo.everyknowCards.get(playerCardInfo.everyknowCards.size()-1));
               }else{
                   playerCardInfo.everyknowCards.add(temp);
               }
           }
            //通知其他人发的明牌
            Player.sendMsg2Player(new ResponseVo("gameService","dealevery",playerCardInfo.everyknowCards),users);
            noticeCanBet(getMaxCardUser(trunNumber));//通知牌点数最大的人可以下注
            curUser.add(playerCardInfo.userId);//添加到
        }
        this.trunNumber += 1;//公开的牌+1
    }

    /**
     * 本局下注，本轮下注
     * @return
     */
    private void addToChip(Long userId,int chip){
        allChip.put(userId,allChip.get(userId)+chip);
        curChip.put(userId,curChip.get(userId)+chip);
    }

    /**
     * 通知其他人这轮谁赢了
     * @param userId
     */
    private void noticeWhoWin(long userId){
        Map<String, Long> result = new HashMap<>();
        result.put("userId",userId);
        ResponseVo vo = new ResponseVo("gameService","whoWin",result);
        Player.sendMsg2Player(vo,users);
    }

    /**
     * 通知其他人这轮积分的最终归属
     */
    private void noticeFinishScores(Map<Long,Integer> allChip){
        Map<String, Map<Long,Integer>> result = new HashMap<>();
        result.put("allChip",allChip);
        ResponseVo vo = new ResponseVo("gameService","finishScores",result);
        Player.sendMsg2Player(vo,users);
    }

    /**
     * 通知其他人分数
     */
    private void noticeOtherScores(){
        Map<String, Map<Long,Integer>> result = new HashMap<>();
        result.put("allChip",allChip);
        ResponseVo vo = new ResponseVo("gameService","otherScores",result);
        Player.sendMsg2Player(vo,users);
    }

    /**
     * 通知可以踢，加注
     * @param userId
     */
    private void noticeCanRaise(long userId){
        Map<String, Long> result = new HashMap<>();
        result.put("userId",userId);
        ResponseVo vo = new ResponseVo("gameService","canRaise",result);
        Player.sendMsg2Player(vo,users);
    }

    /**
     * 通知可以下注
     * @param userId
     */
    private void noticeCanBet(long userId){
        Map<String, Long> result = new HashMap<>();
        result.put("userId",userId);
        ResponseVo vo = new ResponseVo("gameService","canBet",result);
        Player.sendMsg2Player(vo,users);
    }

    /**
     * 通知可以跟注
     * @param userId
     */
    private void noticeCanCall(long userId){
        Map<String, Long> result = new HashMap<>();
        result.put("userId",userId);
        ResponseVo vo = new ResponseVo("gameService","canCall",result);
        Player.sendMsg2Player(vo,users);
    }

    /**
     * 通知其他人弃牌
     * @param userId
     */
    private void noticeOtherFold(long userId){
        Map<String, Long> result = new HashMap<>();
        result.put("userId",userId);
        ResponseVo vo = new ResponseVo("gameService","otherFold",result);
        Player.sendMsg2Player(vo,users);
    }

    /**
     * 下个人
     * @param curId
     * @return
     */
    public long nextTurnId(long curId) {
        int index = aliveUser.indexOf(curId);

        int nextId = index + 1;
        if (nextId >= aliveUser.size()) {
            nextId = 0;
        }
        return aliveUser.get(nextId);
    }

    /**
     * 下一个能反踢的人
     * @param curId
     * @return
     */
    public long nextCanRaiseId(long curId) {
        int index = canRaiseUser.indexOf(curId);

        int nextId = index + 1;
        if (nextId >= canRaiseUser.size()) {
            nextId = 0;
        }
        return canRaiseUser.get(nextId);
    }

    /**
     * 做出条件后判断
     */
    public void branch(){
        if(curUser.isEmpty()){
            if(aliveUser.size()>2){
                canRaiseUser.remove(nextCanRaiseId(currentTurn));//删掉这轮反踢的人，转了一圈，所以下一个人就是当时踢的人
                if(!canRaiseUser.isEmpty()){
                    /*if(tableCards.size()==0 || playerCardInfos.get(aliveUser.get(0)).allCards.size()==5){
                        noticeWhoWin(getWhoWin());
                        Long winner = getWhoWin();
                        for (Long l:allChip.keySet()) {//结算积分
                            if(!l.equals(winner)){
                                allChip.put(winner,allChip.get(winner)+allChip.get(l));
                                allChip.put(l,0);
                            }
                        }
                     noticeFinishScores(allChip);
                    }else{
//                        dealACard();//发牌
                    }*/
                    noticeCanRaise(nextCanRaiseId(currentTurn));//通知下一个可以加注（踢）
                    curUser = aliveUser;
                    curUser.remove(currentTurn);//加注(踢)的人不需要再操作
                }else{//没有可以踢的了
//                    noticeCanRaise(nextCanRaiseId(currentTurn));//通知第一个可以踢
//                    currentTurn = nextTurnId(currentTurn);//下一个人
                    if(tableCards.size()==0 || playerCardInfos.get(aliveUser.get(0)).allCards.size()==5){
                        if(getWhoWin()==-1){
                            this.room.setLastDraw(true);
                            noticeWhoWin(getWhoWin());
                            int temp = 0;
                            for (Long l:allChip.keySet()) {//结算积分
                                temp +=  allChip.get(l);
                            }
                            this.room.setDrawForLeaveChip(temp);
                            endSth();
                        }else{
                            noticeWhoWin(getWhoWin());
                            Long winner = getWhoWin();
                            for (Long l:allChip.keySet()) {//结算积分
                                if(!l.equals(winner)){
                                    allChip.put(winner,allChip.get(winner)+allChip.get(l));
                                    allChip.put(l,0);
                                    this.room.getUserScores().put(l,this.room.getUserScores().get(l)-allChip.get(l));
                                    this.room.getUserScores().put(winner,this.room.getUserScores().get(winner)+allChip.get(l));
                                }
                            }
                            if(this.room.getDrawForLeaveChip()!=0){
                                allChip.put(winner,allChip.get(winner)+this.room.getDrawForLeaveChip());
                            }
                            noticeFinishScores(allChip);
                        }
                    }else{
                        dealACard();//发牌
                        canRaiseUser = aliveUser;//重置可操作人的列表
                        curUser = aliveUser;
                    }
                }
            }
            else if(aliveUser.size()==2){//少于3个人，无限踢
                noticeCanRaise(nextCanRaiseId(currentTurn));//通知第一个可以踢
                currentTurn = nextTurnId(currentTurn);//下一个人
            }else if(aliveUser.size()<2){//是一个人了，直接获胜
                noticeWhoWin(getWhoWin());
                Long winner = getWhoWin();
                for (Long l:allChip.keySet()) {//结算积分
                    if(!l.equals(winner)){
                        allChip.put(winner,allChip.get(winner)+allChip.get(l));
                        allChip.put(l,0);
                    }
                }
                noticeFinishScores(allChip);
            }
            curUser = aliveUser;
        }else{
            currentTurn = nextTurnId(currentTurn);//下一个人
            noticeCanCall(currentTurn);//通知下一个人可以下注
        }
    }



    /**
     * 牌局结束后的相关事宜
     */
    public void endSth(){
        //生成记录
        genRecord();
        room.clearReadyStatus(true);
        sendFinalResult();
    }

    /**
     * 存入数据库
     */
    private void genRecord(){
        Record.RoomRecord roomRecord = new Record.RoomRecord();
        roomRecord.setTime(System.currentTimeMillis());
        roomRecord.setType(room.getCreateType());
        for (long userId : users) {
            PlayerCardInfoTianDaKeng playerCardInfo = playerCardInfos.get(userId);
            User user = room.getUserMap().get(userId);
            Record.UserRecord userRecord = new Record.UserRecord();
            userRecord.setName(user.getUsername());
            userRecord.setScore(userRecord.getScore()+allChip.get(userId));
            userRecord.setUserId(userId);
            userRecord.setRoomId(room.getRoomId());

            roomRecord.addRecord(userRecord);
        }
        //todo 保存记录
        room.getUserMap().forEach((k,v)->v.getRecord().addRoomRecord(roomRecord));

        //加入数据库保存列表
        GameManager.getInstance().getSaveUser2DB().addAll(room.getUserMap().values());
    }

    /**
     *所有牌局都结束
     */
    private void sendFinalResult() {

        if (room.getCurGameNumber() > room.getGameNumber() && room.getDrawForLeaveChip()==0) {
            GameFinalResult gameFinalResult = new GameFinalResult();
            room.getUserScores().forEach((userId,score)->{

                        gameFinalResult.getUserInfos().add(new GameFinalResult.UserInfo(userId,score));

                        //删除玩家房间映射关系
                        GameManager.getInstance().getUserRoom().remove(userId);
                    }
            );
            Player.sendMsg2Player("gameService","gameFinalResult",gameFinalResult,users);

            //删除room
            GameManager.getInstance().removeRoom(room);

        }
    }

    //getter and setter==========================================================================

    public static Logger getLogger() {
        return logger;
    }

    public List<Integer> getCards() {
        return cards;
    }

    public void setCards(List<Integer> cards) {
        this.cards = cards;
    }

    public List<Integer> getTableCards() {
        return tableCards;
    }

    public void setTableCards(List<Integer> tableCards) {
        this.tableCards = tableCards;
    }

    public Map<Long, PlayerCardInfoTianDaKeng> getPlayerCardInfos() {
        return playerCardInfos;
    }

    public void setPlayerCardInfos(Map<Long, PlayerCardInfoTianDaKeng> playerCardInfos) {
        this.playerCardInfos = playerCardInfos;
    }

    public List<Long> getUsers() {
        return users;
    }

    public void setUsers(List<Long> users) {
        this.users = users;
    }

    public Random getRand() {
        return rand;
    }

    public void setRand(Random rand) {
        this.rand = rand;
    }

    public long getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(long currentTurn) {
        this.currentTurn = currentTurn;
    }

    public static int getInitCardNum() {
        return INIT_CARD_NUM;
    }

    public int getTrunNumber() {
        return trunNumber;
    }

    public void setTrunNumber(int trunNumber) {
        this.trunNumber = trunNumber;
    }

    public RoomTanDaKeng getRoom() {
        return room;
    }

    public void setRoom(RoomTanDaKeng room) {
        this.room = room;
    }

    public static int getInitBottomChip() {
        return INIT_BOTTOM_CHIP;
    }

    public Map<Long, Integer> getAllChip() {
        return allChip;
    }

    public void setAllChip(Map<Long, Integer> allChip) {
        this.allChip = allChip;
    }

    public Map<Long, Integer> getCurChip() {
        return curChip;
    }

    public void setCurChip(Map<Long, Integer> curChip) {
        this.curChip = curChip;
    }

    public List<Long> getAliveUser() {
        return aliveUser;
    }

    public void setAliveUser(List<Long> aliveUser) {
        this.aliveUser = aliveUser;
    }

    public List<Long> getCurUser() {
        return curUser;
    }

    public void setCurUser(List<Long> curUser) {
        this.curUser = curUser;
    }

    public List<Long> getCanRaiseUser() {
        return canRaiseUser;
    }

    public void setCanRaiseUser(List<Long> canRaiseUser) {
        this.canRaiseUser = canRaiseUser;
    }


    public int getChip() {
        return chip;
    }

    public void setChip(int chip) {
        this.chip = chip;
    }

    //==========================获取谁赢=================================
    public long getWhoWin(){

        long userId = 0;
        Map<Long,Integer> scoresFour = new HashedMap();
        Map<Long,Integer> scoresThree = new HashedMap();
        Map<Long,Integer> scoresOther = new HashedMap();


        for (PlayerCardInfoTianDaKeng p: playerCardInfos.values()) {
            if(aliveUser.contains(p)){
                if(isFour(p.allCards)!=0){
                    scoresFour.put(p.userId,isFour(p.allCards));
                }else if(isThree(p.allCards)!=0){
                    scoresThree.put(p.userId,isThree(p.allCards));
                }else{
                    scoresOther.put(p.userId,0);
                }
            }

        }

        if(scoresFour.keySet().size()>=1){//第四张
            return CardUtilOfTangDaKeng.getHaveMaxValueOnKeys(scoresFour).get(0);
        }else if(scoresThree.keySet().size()>1){//第三张
            int temp = 0;
            for (Long l:scoresThree.keySet()) {
                if(CardUtilOfTangDaKeng.getAllScores(playerCardInfos.get(l).allCards)>temp){
                    temp = CardUtilOfTangDaKeng.getAllScores(playerCardInfos.get(l).allCards);
                    userId = l;
                }else if(CardUtilOfTangDaKeng.getAllScores(playerCardInfos.get(l).allCards)==temp){
                    userId = -1;
                }
            }
            return userId;
        }else if(scoresThree.keySet().size()==1){
            return CardUtilOfTangDaKeng.getHaveMaxValueOnKeys(scoresThree).get(0);
        }else{
            int temp = 0;
            for (Long l:aliveUser) {
                if(CardUtilOfTangDaKeng.getAllScores(playerCardInfos.get(l).allCards)>temp){
                    temp = CardUtilOfTangDaKeng.getAllScores(playerCardInfos.get(l).allCards);
                    userId = l;
                }else if(CardUtilOfTangDaKeng.getAllScores(playerCardInfos.get(l).allCards)==temp){
                    userId = -1;
                }
            }
            return userId;
        }
    }

    private int isFour(List<Integer> cards) {
        Map<Integer,Integer> map = new HashMap<>();
        for (Integer integer:cards) {
            if(!map.keySet().contains(getCardType(integer))){
                map.put(getCardType(integer),1);
            }else{
                map.put(getCardType(integer),map.get(getCardType(integer))+1);
                if(map.get(getCardType(integer))==4){
                    return  CardUtilOfTangDaKeng.getCardForScore().get(integer);
                }
            }
        }
        return 0;
    }

    private int isThree(List<Integer> cards) {
        Map<Integer,Integer> map = new HashMap<>();
        int temp = 0;
        for (Integer integer:cards) {
            if(!map.keySet().contains(getCardType(integer))){
                map.put(getCardType(integer),1);
            }else{
                map.put(getCardType(integer),map.get(getCardType(integer))+1);
                if(map.get(getCardType(integer))==3){
                    temp = integer;
                }
            }
        }
        if(temp!=0){
            return  CardUtilOfTangDaKeng.getCardForScore().get(temp);
        }else{
            return 0;
        }
    }

}
