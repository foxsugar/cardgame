package com.code.server.cardgame.core.game;

import com.code.server.cardgame.core.CardStruct;
import com.code.server.cardgame.core.CardUtilOfTangDaKeng;
import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.core.PlayerCardInfoTianDaKeng;
import com.code.server.cardgame.response.ResponseVo;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.code.server.cardgame.response.ErrorCodeTDK;

import java.util.*;

/**
 * Created by sunxianping on 2017/3/13.
 */
public class GameTianDaKeng extends Game{
    private static final Logger logger = LoggerFactory.getLogger(GameTianDaKeng.class);

    private static final int INIT_CARD_NUM = 3;
    private static final int MAX_BET_NUM = 5;

    protected List<Integer> cards = new ArrayList<>();//牌

    protected List<Integer> tableCards = new ArrayList<>();//剩余牌
    protected Map<Long,PlayerCardInfoTianDaKeng> playerCardInfos = new HashMap<>();
    protected List<Long> users = new ArrayList<>();
    private Random rand = new Random();

    protected Map<Long,Integer> allChip = new HashedMap();//总下注数
    protected Map<Long,Integer> curChip = new HashedMap();//当前下注数


    private long currentTurn;//当前操作人
    private int chip;//当前操作人
    private int trunNumber;//第几张牌了


    protected List<Long> aliveUser = new ArrayList<>();//存活的人
    protected List<Long> curUser = new ArrayList<>();//本轮的人
    protected List<Long> canRaiseUser = new ArrayList<>();//可以反踢的人

    public void startGame(List<Long> users){
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
        mustBet();
        currentTurn = getMaxCardUser(1);
        noticeCanBet(getMaxCardUser(1));
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
            //通知自己发的2张底牌
            Player.sendMsg2Player(new ResponseVo("gameTDKService","dealmyself",playerCardInfo.myselfCards),playerCardInfo.userId);
            //通知其他人的第三张明牌
            Player.sendMsg2Player(new ResponseVo("gameTDKService","dealevery",playerCardInfo.everyknowCards),users);
        }

        //底牌
        tableCards.addAll(cards);
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

        if(chip > MAX_BET_NUM){//下注错误
            return ErrorCodeTDK.MORE_BET;
        }
        this.chip = chip;
        addToChip(player.getUserId(),chip);//添加积分
        curUser.remove(currentTurn);//本轮操作完删除
        currentTurn = nextTurnId(currentTurn);//下一个人
        noticeCanCall(currentTurn);//通知下一个人可以下注

        player.sendMsg(new ResponseVo("gameTDKService","bet",0));
        return 0;
    }


    /**
     * 必须下底注
     */
    private void mustBet(){
        for (Long user : users) {
            allChip.put(user,1);
        }
        //通知下底注
        Player.sendMsg2Player(new ResponseVo("gameTDKService","mustBet",allChip),users);

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

        if(chip > MAX_BET_NUM){//下注错误
            return ErrorCodeTDK.MORE_BET;
        }
        addToChip(player.getUserId(),chip);//添加积分
        curUser.remove(currentTurn);//本轮操作完删除
        if(curUser.isEmpty()){
            if(aliveUser.size()>2){
                canRaiseUser.remove(nextCanRaiseId(currentTurn));//删掉这轮反踢的人，转了一圈，所以下一个人就是当时踢的人
                if(canRaiseUser.isEmpty()){//没有可以踢的了
                    dealACard();//发牌
                }else{
                    noticeCanRaise(nextCanRaiseId(currentTurn));//通知第一个可以踢
                }
            }
            else if(aliveUser.size()==2){//少于3个人，无限踢
                noticeCanRaise(nextCanRaiseId(currentTurn));//通知第一个可以踢
            }
            curUser = aliveUser;
        }else{
            noticeCanCall(currentTurn);//通知下一个人可以下注
        }
        currentTurn = nextTurnId(currentTurn);//下一个人

        player.sendMsg(new ResponseVo("gameTDKService","call",0));
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

        if(chip > MAX_BET_NUM){//下注错误
            return ErrorCodeTDK.MORE_BET;
        }
        this.chip = chip;
        addToChip(player.getUserId(),chip);//添加积分
        noticeCanCall(currentTurn);//通知下一个人可以下注
        curUser.remove(currentTurn);//本轮操作完删除
        canRaiseUser.remove(currentTurn);//每个人可以反踢一次，踢完删除
        currentTurn = nextTurnId(currentTurn);//下一个人

        player.sendMsg(new ResponseVo("gameTDKService","raise",0));
        return 0;
    }

    /**
     * 不跟,不踢
     * @param player
     * @return
     */
    public int pass(Player player){
        logger.info(player.getUser().getAccount() +"  不踢 ");

        aliveUser.remove(currentTurn);
        curUser.remove(currentTurn);//本轮操作完删除

        if(curUser.isEmpty()){
            if(aliveUser.size()>2){
                canRaiseUser.remove(nextCanRaiseId(currentTurn));//删掉这轮反踢的人
                if(canRaiseUser.isEmpty()){//没有可以踢的了
                    dealACard();//发牌
                }else{
                    noticeCanRaise(nextCanRaiseId(currentTurn));//通知第一个可以踢
                }
            }
            else if(aliveUser.size()==2){//少于3个人，无限踢
                noticeCanRaise(nextCanRaiseId(currentTurn));//通知第一个可以踢
            }
            curUser = aliveUser;
        }else{
            noticeCanCall(currentTurn);//通知下一个人可以下注
        }
        currentTurn = nextTurnId(currentTurn);//下一个人

        player.sendMsg(new ResponseVo("gameTDKService","pass",0));
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
        noticeCanCall(currentTurn);//通知下一个人可以下注


        if(curUser.isEmpty()){
            if(aliveUser.size()>2){
                canRaiseUser.remove(nextCanRaiseId(currentTurn));//删掉这轮反踢的人
                if(canRaiseUser.isEmpty()){//没有可以踢的了
                    dealACard();//发牌
                }else{
                    noticeCanRaise(nextCanRaiseId(currentTurn));//通知第一个可以踢
                }
            }
            else if(aliveUser.size()==2){//少于3个人，无限踢
                noticeCanRaise(nextCanRaiseId(currentTurn));//通知第一个可以踢
            }
            curUser = aliveUser;
        }else{
            noticeCanCall(currentTurn);//通知下一个人可以下注
        }

        currentTurn = nextTurnId(currentTurn);//下一个人

        player.sendMsg(new ResponseVo("gameTDKService","fold",0));
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
               }else if(tableCards.size() == 1){
                   temp = tableCards.get(0);
                   playerCardInfo.everyknowCards.add(tableCards.remove(0));
               }else{
                   playerCardInfo.everyknowCards.add(temp);
               }
           }
            //通知其他人发的明牌
            Player.sendMsg2Player(new ResponseVo("gameTDKService","dealevery",playerCardInfo.everyknowCards),users);
            this.trunNumber += 1;//公开的牌+1
            noticeCanBet(getMaxCardUser(trunNumber));//通知牌点数最大的人可以下注
        }
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
     * 通知可以踢，加注
     * @param userId
     */
    private void noticeCanRaise(long userId){
        Map<String, Long> result = new HashMap<>();
        result.put("userId",userId);
        ResponseVo vo = new ResponseVo("gameTDKService","canRaise",result);
        Player.sendMsg2Player(vo,users);
    }

    /**
     * 通知可以下注
     * @param userId
     */
    private void noticeCanBet(long userId){
        Map<String, Long> result = new HashMap<>();
        result.put("userId",userId);
        ResponseVo vo = new ResponseVo("gameTDKService","canBet",result);
        Player.sendMsg2Player(vo,users);
    }

    /**
     * 通知可以跟注
     * @param userId
     */
    private void noticeCanCall(long userId){
        Map<String, Long> result = new HashMap<>();
        result.put("userId",userId);
        ResponseVo vo = new ResponseVo("gameTDKService","canCall",result);
        Player.sendMsg2Player(vo,users);
    }

    /**
     * 通知其他人弃牌
     * @param userId
     */
    private void noticeOtherFold(long userId){
        Map<String, Long> result = new HashMap<>();
        result.put("userId",userId);
        ResponseVo vo = new ResponseVo("gameTDKService","otherFold",result);
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
}
