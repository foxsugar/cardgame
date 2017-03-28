package com.code.server.cardgame.core.game;

import com.code.server.cardgame.core.CardStruct;
import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.core.PlayerCardInfo;
import com.code.server.cardgame.response.ErrorCode;
import com.code.server.cardgame.response.ResponseVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by sunxianping on 2017/3/13.
 */
public class GameTianDaKeng extends Game{
    private static final Logger logger = LoggerFactory.getLogger(GameTianDaKeng.class);

    private static final int INIT_CARD_NUM = 3;

    protected List<Integer> cards = new ArrayList<>();//牌

    protected List<Integer> tableCards = new ArrayList<>();//剩余牌
    protected Map<Long,PlayerCardInfo> playerCardInfos = new HashMap<>();
    protected List<Long> users = new ArrayList<>();
    private Random rand = new Random();

    private long currentTurn;//当前操作人


    public void startGame(List<Long> users){
        init(users);
    }
    public void init(List<Long> users){
        //初始化玩家
        for(Long uid : users){
            PlayerCardInfo playerCardInfo = new PlayerCardInfo();
            playerCardInfo.userId = uid;
            playerCardInfos.put(uid,playerCardInfo);
        }
        this.users.addAll(users);
        shuffle();
        deal();

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
        for(PlayerCardInfo playerCardInfo : playerCardInfos.values()){
            for(int i=0;i<INIT_CARD_NUM;i++){
                playerCardInfo.cards.add(cards.remove(0));
            }
            //通知发牌
            Player.sendMsg2Player(new ResponseVo("gameService","deal",playerCardInfo.cards),playerCardInfo.userId);
        }

        //底牌
        tableCards.addAll(cards);

    }





    /**
     * 开始打牌
     * @param dizhu
     */
    private void startPlay(long dizhu){

    }




    /**
     * 通知可以叫地主
     * @param userId
     */
    private void noticeCanJiao(long userId){
        Map<String, Long> result = new HashMap<>();
        result.put("userId",userId);
        ResponseVo vo = new ResponseVo("gameService","canjiao",result);
        Player.sendMsg2Player(vo,users);
    }

    /**
     * 通知可以抢地主
     * @param userId
     */
    private void noticeCanQiang(long userId) {
        Map<String, Long> result = new HashMap<>();
        result.put("userId",userId);
        ResponseVo vo = new ResponseVo("gameService","canqiang",result);
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

    public Map<Long, PlayerCardInfo> getPlayerCardInfos() {
        return playerCardInfos;
    }

    public void setPlayerCardInfos(Map<Long, PlayerCardInfo> playerCardInfos) {
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
