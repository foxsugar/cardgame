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
        shuffle();
        deal();
        mustBet();
        currentTurn = getMaxCardUser(1);
        noticeCanBet(getMaxCardUser(1));
    }

    /**
     * 出牌
     * @param player
     */
    public int play(Player player,CardStruct cardStruct){
        return 0;
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
    protected void deal(){
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
    }

    /**
     * 跟注
     * @param player
     * @return
     */
    public int raise(Player player){

        return 0;
    }

    /**
     * 加注，踢
     * @param player
     * @return
     */
    public int call(Player player){

        return 0;
    }
    /**
     * 不跟
     * @param player
     * @return
     */
    public int pass(Player player){

        return 0;
    }
    /**
     * 弃牌
     * @param player
     * @return
     */
    public int fold(Player player){

        return 0;
    }

    /**
     * 发牌
     * @param player
     * @return
     */
    public int deal(Player player){

        return 0;
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
     * 下个人
     * @param curId
     * @return
     */
    public long nextTurnId(long curId) {
        int index = users.indexOf(curId);

        int nextId = index + 1;
        if (nextId >= users.size()) {
            nextId = 0;
        }
        return users.get(nextId);
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
}
