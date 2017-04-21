package com.code.server.cardgame.core.game;

import com.code.server.cardgame.core.*;
import com.code.server.cardgame.core.room.Room;
import com.code.server.cardgame.core.room.RoomTanDaKeng;
import com.code.server.cardgame.response.GameFinalResult;
import com.code.server.cardgame.response.ResponseVo;
import com.code.server.cardgame.utils.SpringUtil;
import com.code.server.db.Service.UserService;
import com.code.server.db.model.Record;
import com.code.server.db.model.User;
import com.code.server.db.model.UserInfo;
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

    private static final Double INIT_BOTTOM_CHIP = 1.0;//底注
    private static final int INIT_CARD_NUM = 3;
    private static final Double MAX_BET_NUM = 5.0;
    private static final Double DOUBLE_MAX_BET_NUM = 10.0;//烂锅下一局的最大注上限

    protected List<Integer> cards = new ArrayList<>();//牌

    public Map<Long,List<Integer>> everyknowCardsAndUserId = new HashMap<>();//每个人手上的牌(明)

    protected List<Integer> tableCards = new ArrayList<>();//剩余牌
    protected Map<Long,PlayerCardInfoTianDaKeng> playerCardInfos = new HashMap<>();
    protected List<Long> users = new ArrayList<>();
    private Random rand = new Random();

    protected Map<Long,Double> allChip = new HashMap<>();//总下注数
    protected Map<Long,Double> curChip = new HashMap<>();//当前下注数

    /*玩家状态 ：等待操作 0
    等待下注 10 可以下注 11 已经下注 12
    等待加注 20 可以加注 21 已经加注 22
    可以正踢 311  可以反踢312   已经踢 32
    已经过 40
    已经弃牌 50
     */
    protected Map<Long,Integer> gameuserStatus = new HashMap<>();

    private long currentTurn;//当前操作人
    private int chip;//下注
    private int trunNumber;//第几张牌了


    protected List<Long> aliveUser = new ArrayList<>();//存活的人
    protected List<Long> curUser = new ArrayList<>();//本轮的人
    protected List<Long> canRaiseUser = new ArrayList<>();//可以反踢的人

    protected Room room;//房间

    public void startGame(List<Long> users,Room room){
        this.room = room;
        init(users);
    }
    public void init(List<Long> users){

        //初始化玩家
        for(Long uid : users){
            PlayerCardInfoTianDaKeng playerCardInfo = new PlayerCardInfoTianDaKeng();
            playerCardInfo.userId = uid;
            playerCardInfos.put(uid,playerCardInfo);
            gameuserStatus.put(uid,0);
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
        for (int i = 37; i < 53; i++) {
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
                    int temp = cards.get(0);
                    cards.remove(0);
                    playerCardInfo.everyknowCards.add(temp);
                    ArrayList<Integer> templist = new ArrayList<>();
                    templist.add(temp);
                    everyknowCardsAndUserId.put(playerCardInfo.userId,templist);
                }else{
                    break;
                }
            }
            playerCardInfo.allCards.addAll(playerCardInfo.myselfCards);
            playerCardInfo.allCards.addAll(playerCardInfo.everyknowCards);
        }
        for(PlayerCardInfoTianDaKeng playerCardInfo : playerCardInfos.values()){
            noticeDealevery(playerCardInfo.userId,playerCardInfo.allCards,everyknowCardsAndUserId);
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
            curChip.put(user,1.0);
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
            if(temp < CardUtilOfTangDaKeng.getCardForScore().get(playerCardInfoTianDaKeng.everyknowCards.get(playerCardInfoTianDaKeng.everyknowCards.size()-1))){
                temp = CardUtilOfTangDaKeng.getCardForScore().get(playerCardInfoTianDaKeng.everyknowCards.get(playerCardInfoTianDaKeng.everyknowCards.size()-1));
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

        for (Long l:users) {//去掉底注
            curChip.put(l,0.0);
        }

        gameuserStatus.put(player.getUserId(),12);
        gameuserStatus.put(nextTurnId(currentTurn),11);

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
        noticeBetFinish(player.getUserId(),chip);
        player.sendMsg(new ResponseVo("gameService","bet",0));
        return 0;
    }

    /**
     * 跟注
     * @param player
     * @return
     */
    public int call(Player player){

        gameuserStatus.put(player.getUserId(),12);
        gameuserStatus.put(nextTurnId(currentTurn),11);

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

        noticeCallFinish(player.getUserId(),chip);
        branch();
        player.sendMsg(new ResponseVo("gameService","call",0));
        return 0;
    }

    /**
     * 加注，踢
     * @param player
     * @return
     */
    public int raise(Player player,int chip){

        gameuserStatus.put(player.getUserId(),32);
        gameuserStatus.put(nextTurnId(currentTurn),21);

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
        if(canRaiseUser.size()!=2){//2人无限踢不删
            canRaiseUser.remove(currentTurn);
        }
        currentTurn = nextTurnId(currentTurn);//下一个人
        noticeCanCall(currentTurn);//通知下一个人可以下注
        noticeRaiseFinish(player.getUserId(),chip);
        player.sendMsg(new ResponseVo("gameService","raise",0));
        return 0;
    }

    /**
     * 不跟,不踢
     * @param player
     * @return
     */
    public int pass(Player player){

        gameuserStatus.put(player.getUserId(),40);
        if(canRaiseUser.containsAll(aliveUser)){
            gameuserStatus.put(nextTurnId(currentTurn),311);
        }else{
            gameuserStatus.put(nextTurnId(currentTurn),312);
        }

        logger.info(player.getUser().getAccount() +"  不踢 ");

        //curUser.remove(currentTurn);//本轮操作完删除
        curUser.removeAll(curUser);
        canRaiseUser.remove(currentTurn);
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

        gameuserStatus.put(player.getUserId(),50);
        gameuserStatus.put(nextTurnId(currentTurn),11);

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
                   temp = tableCards.get(0);
                   tableCards.remove(0);
                   playerCardInfo.everyknowCards.add(temp);
                   playerCardInfo.allCards.add(playerCardInfo.everyknowCards.get(playerCardInfo.everyknowCards.size()-1));
                   List<Integer> list =everyknowCardsAndUserId.get(playerCardInfo.userId);
                   list.add(temp);
                   everyknowCardsAndUserId.put(playerCardInfo.userId,list);
               }else if(tableCards.size() == 1){
                   temp = tableCards.get(0);
                   playerCardInfo.everyknowCards.add(tableCards.remove(0));
                   playerCardInfo.allCards.add(playerCardInfo.everyknowCards.get(playerCardInfo.everyknowCards.size()-1));
               }else{
                   playerCardInfo.everyknowCards.add(temp);
               }
           }
            //通知其他人发的明牌
            //Player.sendMsg2Player(new ResponseVo("gameService","dealevery",playerCardInfo.everyknowCards),users);
            //noticeDealevery(playerCardInfo.userId,playerCardInfo.allCards,everyknowCardsAndUserId);

        }
        gameuserStatus.put(getMaxCardUser(trunNumber),12);
        long tempMax = getMaxCardUser(trunNumber);
        noticeCanBet(tempMax);//通知牌点数最大的人可以下注
        currentTurn = tempMax;
        noticeEveryCards(playerCardInfos);
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
     * 通知自己人牌
     * @param userId
     */
    private void noticeDealevery(long userId,List myselfCards,Map everyknowCards){
        Map<String, Object> result = new HashMap<>();
        result.put("userId",userId);
        result.put("myselfCards",myselfCards);
        result.put("everyknowCards",everyknowCards);
        ResponseVo vo = new ResponseVo("gameService","dealevery",result);
        Player.sendMsg2Player(vo,userId);
    }

    /**
     * 通知其他人人牌
     */
    private void noticeEveryCards(Map<Long,PlayerCardInfoTianDaKeng> playerCardInfos){
        Map<String, Object> result = new HashMap<>();
        Map<Long,List<Integer>> cardsMap = new HashMap<>();
        for (PlayerCardInfoTianDaKeng playerCardInfoTianDaKeng :playerCardInfos.values()) {
            cardsMap.put(playerCardInfoTianDaKeng.getUserId(),playerCardInfoTianDaKeng.everyknowCards);
        }
        result.put("cardsMap",cardsMap);
        ResponseVo vo = new ResponseVo("gameService","everyCards",result);
        Player.sendMsg2Player(vo,users);
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
    private void noticeFinishScores(Map<Long,Double> allChip){
        Map<String, Object> result = new HashMap<>();
        result.put("winUserId",getWhoWin());
        result.put("allChip",allChip);
        ResponseVo vo = new ResponseVo("gameService","finishScores",result);
        Player.sendMsg2Player(vo,users);
    }

    /**
     * 通知其他人分数
     */
    private void noticeOtherScores(){
        Map<String, Map<Long,Double>> result = new HashMap<>();
        result.put("allChip",allChip);
        ResponseVo vo = new ResponseVo("gameService","otherScores",result);
        Player.sendMsg2Player(vo,users);
    }

    /**
     * 通知可以踢，加注
     * @param userId
     */
    private void noticeCanRaise(long userId){


        for (Long l:users) {//清空当前下注
            curChip.put(l,0.0);
        }
        if(canRaiseUser.containsAll(aliveUser)){
            gameuserStatus.put(nextTurnId(currentTurn),311);
        }else{
            gameuserStatus.put(nextTurnId(currentTurn),312);
        }

        Map<String, Long> result = new HashMap<>();
        result.put("userId",userId);
        ResponseVo vo = new ResponseVo("gameService","canRaise",result);
        Player.sendMsg2Player(vo,users);
    }

    /**
     * 通知可以踢，其他人已经加注
     * @param userId
     */
    private void noticeRaiseFinish(long userId,int chip){

        Map<String, Long> result = new HashMap<>();
        result.put("userId",userId);
        result.put("chip",Long.parseLong(chip+""));
        ResponseVo vo = new ResponseVo("gameService","raiseFinish",result);
        Player.sendMsg2Player(vo,users);
    }

    /**
     * 通知可以下注
     * @param userId
     */
    private void noticeCanBet(long userId){
        gameuserStatus.put(userId,11);
        Map<String, Long> result = new HashMap<>();
        result.put("userId",userId);
        ResponseVo vo = new ResponseVo("gameService","canBet",result);
        Player.sendMsg2Player(vo,users);
    }

    /**
     * 通知可以其他人已经下注
     * @param userId
     */
    private void noticeBetFinish(long userId,int chip){
        Map<String, Long> result = new HashMap<>();
        result.put("userId",userId);
        result.put("chip",Long.parseLong(chip+""));
        ResponseVo vo = new ResponseVo("gameService","betFinish",result);
        Player.sendMsg2Player(vo,users);
    }

    /**
     * 通知可以其他人已经下注
     * @param userId
     */
    private void noticeCallFinish(long userId,int chip){
        Map<String, Long> result = new HashMap<>();
        result.put("userId",userId);
        result.put("chip",Long.parseLong(chip+""));
        ResponseVo vo = new ResponseVo("gameService","callFinish",result);
        Player.sendMsg2Player(vo,users);
    }

    /**
     * 通知可以跟注
     * @param userId
     */
    private void noticeCanCall(long userId){

        gameuserStatus.put(userId,11);

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

        gameuserStatus.put(userId,50);

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
                if(!canRaiseUser.isEmpty()) {
                    if(canRaiseUser.size()!=aliveUser.size()){

                        List<Long> list1 = new ArrayList();
                        List<Long> list2 = new ArrayList<>();
                        list1.addAll(aliveUser);
                        list2.addAll(canRaiseUser);
                        list2.add(currentTurn);
                        list1.retainAll(list2);

                        int index = list1.indexOf(currentTurn);
                        int nextId = index + 1;
                        if (nextId >= list1.size()) {
                            nextId = 0;
                        }
                        Long temp = list1.get(nextId);

                        //Long temp = nextCanRaiseId(currentTurn);
                        noticeCanRaise(temp);//通知下一个可以加注（踢）
                        canRaiseUser.remove(temp);
                        currentTurn = temp;//下一个人

                        List<Long> list = new ArrayList();
                        list.addAll(aliveUser);
                        curUser = list;
                    }else if(canRaiseUser.size()==aliveUser.size()){
                        long tempCanRaiseId = nextCanRaiseId(currentTurn);
                        noticeCanRaise(tempCanRaiseId);
                        currentTurn = tempCanRaiseId;//下一个人

                    }
                }else{//没有可以踢的了
                    if(tableCards.size()==0 || playerCardInfos.get(aliveUser.get(0)).allCards.size()==5){
                        if(getWhoWin()==-1){
                            this.room.setLastDraw(true);
                            int temp = 0;
                            for (Long l:allChip.keySet()) {//结算积分
                                temp +=  allChip.get(l);
                            }
                            this.room.setDrawForLeaveChip(temp);
                            noticeFinishScores(allChip);
                            endSth();
                        }else{
                            Long winner = getWhoWin();
                            for (Long l:allChip.keySet()) {//结算积分
                                if(!l.equals(winner)){
                                    allChip.put(winner,allChip.get(winner)+allChip.get(l));
                                    allChip.put(l,-allChip.get(l));
                                    this.room.getUserScores().put(l,(this.room.getUserScores().get(l)-allChip.get(l))*this.room.getMultiple()/100);
                                    this.room.getUserScores().put(winner,(this.room.getUserScores().get(winner)+allChip.get(l))*this.room.getMultiple()/100);
                                }
                            }
                            if(this.room.getDrawForLeaveChip()!=0){
                                allChip.put(winner,allChip.get(winner)+this.room.getDrawForLeaveChip());
                            }
                            noticeFinishScores(allChip);
                            endSth();
                        }
                    }else{
                        dealACard();//发牌
                        List<Long> list = new ArrayList();
                        list.addAll(aliveUser);
                        canRaiseUser = list;//重置可操作人的列表
                        curUser = list;
                    }
                }
            }
            else if(aliveUser.size()==2){//少于3个人，无限踢
                noticeCanRaise(nextCanRaiseId(currentTurn));//通知第一个可以踢
                currentTurn = nextTurnId(currentTurn);//下一个人
            }else if(aliveUser.size()<2){//是一个人了，直接获胜
                Long winner = getWhoWin();
                for (Long l:allChip.keySet()) {//结算积分
                    if(!l.equals(winner)){
                        allChip.put(winner,allChip.get(winner)+allChip.get(l));
                        allChip.put(l,0.0);
                        this.room.getUserScores().put(l,(this.room.getUserScores().get(l)-allChip.get(l))*this.room.getMultiple()/100);
                        this.room.getUserScores().put(winner,(this.room.getUserScores().get(winner)+allChip.get(l))*this.room.getMultiple()/100);
                    }
                }
                noticeFinishScores(allChip);
                endSth();
            }
            List<Long> list = new ArrayList<>();
            list.addAll(aliveUser);
            curUser = list;
        }else{
            if(aliveUser.size()==canRaiseUser.size() && curUser.size()==canRaiseUser.size()){//第一个人扣牌
                if(aliveUser.size()!=1){
                    List<Long> list1 = new ArrayList<>();
                    List<Long> list2 = new ArrayList<>();
                    list1.addAll(users);
                    list2.addAll(aliveUser);
                    list2.add(currentTurn);
                    list1.retainAll(list2);

                    int index = list1.indexOf(currentTurn);
                    int nextId = index + 1;
                    if (nextId >= list1.size()) {
                        nextId = 0;
                    }
                    Long temp = list1.get(nextId);
                    currentTurn = temp;//下一个人
                    noticeCanBet(temp);//通知下一个人可以下注
                }else{
                    Long winner = getWhoWin();
                    for (Long l:allChip.keySet()) {//结算积分
                        if(!l.equals(winner)){
                            allChip.put(winner,allChip.get(winner)+allChip.get(l));
                            allChip.put(l,0.0);
                            this.room.getUserScores().put(l,(this.room.getUserScores().get(l)-allChip.get(l))*this.room.getMultiple()/100);
                            this.room.getUserScores().put(winner,(this.room.getUserScores().get(winner)+allChip.get(l))*this.room.getMultiple()/100);
                        }
                    }
                    noticeFinishScores(allChip);
                    endSth();
                }
            }else{//第一个人不扣
                currentTurn = nextTurnId(currentTurn);//下一个人
                noticeCanCall(currentTurn);//通知下一个人可以下注
            }
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
        UserService userService = SpringUtil.getBean(UserService.class);
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

            UserInfo userInfo = user.getUserInfo();
            userInfo.setTotalPlayGameNumber(userInfo.getTotalPlayGameNumber()+1);
            user.setUserInfo(userInfo);

            userService.save(user);
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

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public static Double getInitBottomChip() {
        return INIT_BOTTOM_CHIP;
    }

    public Map<Long, Double> getAllChip() {
        return allChip;
    }

    public void setAllChip(Map<Long, Double> allChip) {
        this.allChip = allChip;
    }

    public Map<Long, Double> getCurChip() {
        return curChip;
    }

    public void setCurChip(Map<Long, Double> curChip) {
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

    public Map<Long, Integer> getGameuserStatus() {
        return gameuserStatus;
    }

    public void setGameuserStatus(Map<Long, Integer> gameuserStatus) {
        this.gameuserStatus = gameuserStatus;
    }

    //==========================获取谁赢=================================
    public long getWhoWin(){

        long userId = 0;
        Map<Long,Integer> scoresFour = new HashMap();
        Map<Long,Integer> scoresThree = new HashMap();
        Map<Long,Integer> scoresOther = new HashMap();


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
