package com.code.server.cardgame.core.game;

import com.code.server.cardgame.core.*;
import com.code.server.cardgame.core.room.Room;
import com.code.server.cardgame.response.ErrorCode;
import com.code.server.cardgame.response.GameResultDouDizhu;
import com.code.server.cardgame.response.PlayerCardInfoVo;
import com.code.server.cardgame.response.ResponseVo;
import com.code.server.db.model.Record;
import com.code.server.db.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by sunxianping on 2017/3/13.
 */
public class GameDouDiZhu extends Game{
    private static final Logger logger = LoggerFactory.getLogger(GameDouDiZhu.class);

    private static final int INITCARDNUM = 16;
    private static final int STEP_JIAO_DIZHU = 1;
    private static final int STEP_QIANG_DIZHU = 2;
    private static final int STEP_PLAY = 3;

    protected List<Integer> cards = new ArrayList<>();//牌
    protected List<Integer> disCards = new ArrayList<>();//丢弃的牌
    protected List<Integer> tableCards = new ArrayList<>();//底牌
    protected Map<Long,PlayerCardInfo> playerCardInfos = new HashMap<>();
    protected List<Long> users = new ArrayList<>();
    private Random rand = new Random();
    protected long dizhu;//地主
    protected Set<Long> chooseJiaoSet = new HashSet<>();
    protected Set<Long> chooseQiangSet = new HashSet<>();
    protected Set<Long> bujiaoSet = new HashSet<>();

//    protected CardStruct currentCardStruct = new CardStruct();// 当前这个人出的牌
    protected int lasttype = 0;//上一个人出牌的类型

    private long canJiaoUser;//可以叫地主的人
    private long canQiangUser;//可以抢地主的人
    private long jiaoUser;//叫的人
    private long qiangUser;//抢的人

    private long playTurn;//该出牌的人

    protected CardStruct lastCardStruct;//上一个人出的牌

    private int step;//步骤

    private int zhaCount;
    private int multiple = 1;
    private Room room;
    private boolean isSpring;
    private Set<Long> userPlayCount = new HashSet<>();



    public void startGame(List<Long> users,Room room){
        this.room = room;
        init(users,room.getBankerId());
    }
    public void init(List<Long> users,long dizhuUser){
        //初始化玩家
        for(Long uid : users){
            PlayerCardInfo playerCardInfo = new PlayerCardInfo();
            playerCardInfo.userId = uid;
            playerCardInfos.put(uid,playerCardInfo);
        }
        this.users.addAll(users);


        shuffle();
        deal();
        chooseDizhu(dizhuUser);




    }

    /**
     * 出牌
     * @param player
     */
    public int play(Player player,CardStruct cardStruct){
        PlayerCardInfo playerCardInfo = playerCardInfos.get(player.getUserId());
        //不可出牌
        if(!playerCardInfo.checkPlayCard(lastCardStruct,cardStruct,lasttype)){
            return ErrorCode.CAN_NOT_PLAY;
        }

//        userPlayCount.add(player.getUserId());
        playerCardInfo.setPlayCount(playerCardInfo.getPlayCount() + 1);

        long nextUserCard = nextTurnId(cardStruct.getUserId()); //下一个出牌的人

        cardStruct.setNextUserId(nextUserCard);
        playTurn = nextUserCard;

        Player.sendMsg2Player(new ResponseVo("gameService","playResponse",cardStruct),this.users);
        lasttype = cardStruct.getType();//保存这次出牌的类型
        lastCardStruct = cardStruct;//保存这次出牌的牌型

        //删除牌
        playerCardInfo.cards.removeAll(cardStruct.getCards());

         if(zhaCount < room.getMultiple()){
            if(cardStruct.getType()==CardStruct.type_炸){
                List<Integer> cards = cardStruct.getCards();
                    if(cards.size()==4 && CardUtil.getTypeByCard(cards.get(0)) == 0 && CardUtil.getTypeByCard(cards.get(cards.size()-1))==0){ //3333
                        zhaCount += 1;//记录炸的数量
                        multiple *= 8;//记录倍数
                    }else{ //除4个三的炸
                        zhaCount += 1;//记录炸的数量
                        multiple *= 2;//记录倍数
                    }
            }else if(cardStruct.getType()==CardStruct.type_火箭){
                zhaCount += 1;//记录炸的数量
                multiple *= 2;//记录倍数
            }
         }

         //牌打完
        if (playerCardInfo.cards.size() == 0) {
            PlayerCardInfo playerCardInfoDizhu = playerCardInfos.get(dizhu);
            //是否是春天
            if (userPlayCount.size() == 1 || playerCardInfoDizhu.getPlayCount() == 1) {
                isSpring = true;
                multiple *= 2;
            }

            compute(playerCardInfo.getUserId() == dizhu);

            sendResult(false,playerCardInfo.getUserId() == dizhu);

            //生成记录
            genRecord();

            room.clearReadyStatus(true);
        }
        player.sendMsg("gameService","play",0);
        return 0;
    }


    public int pass(Player player){
        playTurn = nextTurnId(player.getUserId());
        Map<String, Long> rs = new HashMap<>();
        rs.put("userId",player.getUserId());
        rs.put("nextUserId", playTurn);

        Player.sendMsg2Player("gameService","passResponse",rs,this.users);

        player.sendMsg("gameService","pass",0);
        return 0;
    }



    /**
     * 洗牌
     */
    protected void shuffle(){
        for(int i=1;i<=54;i++){
            cards.add(i);
        }
        //去掉两张2
        cards.remove((Integer)7);
        cards.remove((Integer)8);
        Collections.shuffle(cards);
    }

    /**
     * 发牌
     */
    protected void deal(){
        for(PlayerCardInfo playerCardInfo : playerCardInfos.values()){
            for(int i=0;i<INITCARDNUM;i++){
                playerCardInfo.cards.add(cards.remove(0));
            }
            //通知发牌
            Player.sendMsg2Player(new ResponseVo("gameService","deal",playerCardInfo.cards),playerCardInfo.userId);
        }

        //底牌
        tableCards.addAll(cards);

    }


    /**
     * 选叫地主
     * @param lastJiaoUser
     */
    protected void chooseDizhu(long lastJiaoUser) {
        step = STEP_JIAO_DIZHU;
        long canJiao = 0;
        //随机叫地主
        if (lastJiaoUser == 0) {
            int index = rand.nextInt(3);
            canJiao = users.get(index);
        } else {
            canJiao = nextTurnId(lastJiaoUser);
        }
        canJiaoUser = canJiao;

        //
        noticeCanJiao(canJiaoUser);


        //下次叫的人
        room.setBankerId(nextTurnId(canJiaoUser));

    }

    /**
     * 叫地主
     * @param player
     * @param isJiao
     * @return
     */
    public int jiaoDizhu(Player player,boolean isJiao){

        logger.info(player.getUser().getAccount() +"  叫地主 "+ isJiao);
        if (canJiaoUser != player.getUserId()) {
            return ErrorCode.CAN_NOT_JIAO_TURN;
        }
        //叫地主列表
        chooseJiaoSet.add(player.getUserId());

        //不叫 下个人能叫
        if (!isJiao) {
            bujiaoSet.add(player.getUserId());
            if (bujiaoSet.size() >= users.size()) {

                //todo 重新洗牌
                sendResult(true,false);
                room.clearReadyStatus(false);

            } else {
                //todo
                long nextJiao = nextTurnId(player.getUserId());
//                long nextJiao = users.get(2);

                canJiaoUser = nextJiao;
                noticeCanJiao(nextJiao);
            }
        } else {//叫了 开始抢
            jiaoUser = player.getUserId();
            //第三个人叫的 直接开始游戏
            if (chooseJiaoSet.size() >= users.size()) {
                startPlay(jiaoUser);
            } else {

                step = STEP_QIANG_DIZHU;
                long nextId = nextTurnId(player.getUserId());
                this.canQiangUser = nextId;
                noticeCanQiang(nextId);
            }

        }

        player.sendMsg(new ResponseVo("gameService","jiaoDizhu",0));
        return 0;
    }

    private void compute(boolean isDizhuWin){

        double subScore = 0;
        int s = isDizhuWin?-1:1;
        //地主
        PlayerCardInfo playerCardInfoDizhu = playerCardInfos.get(dizhu);
        if (playerCardInfoDizhu.isQiang()) {
            multiple *= 2;
        }
        for(PlayerCardInfo playerCardInfo : playerCardInfos.values()){
            //不是地主 扣分
            if(dizhu != playerCardInfo.getUserId()){
                double score = multiple * s;
                if (playerCardInfo.isQiang()) {
                    score *=2;
                }
                subScore += score;
                playerCardInfo.setScore(score);
                room.addUserSocre(playerCardInfo.getUserId(),score);



            }
        }

        playerCardInfoDizhu.setScore(-subScore);
        room.addUserSocre(dizhu,-subScore);

    }

    private void sendResult(boolean isReopen,boolean isDizhuWin){
        GameResultDouDizhu gameResultDouDizhu = new GameResultDouDizhu();
        gameResultDouDizhu.setMultiple(multiple);
        gameResultDouDizhu.setSpring(isSpring);
        gameResultDouDizhu.setDizhuWin(isDizhuWin);
        gameResultDouDizhu.setReopen(isReopen);
        for (PlayerCardInfo playerCardInfo : playerCardInfos.values()) {
            gameResultDouDizhu.getPlayerCardInfos().add(new PlayerCardInfoVo(playerCardInfo));

        }
        Player.sendMsg2Player("gameService","gameResult",gameResultDouDizhu,users);
    }

    private void genRecord(){
        Record.RoomRecord roomRecord = new Record.RoomRecord();
        for (long userId : users) {
            PlayerCardInfo playerCardInfo = playerCardInfos.get(userId);
            User user = room.getUserMap().get(userId);
            Record.UserRecord userRecord = new Record.UserRecord();
            userRecord.setName(user.getUsername());
            userRecord.setScore(playerCardInfo.getScore());
            userRecord.setUserId(userId);
            roomRecord.addRecord(userRecord);


        }
        room.getUserMap().forEach((k,v)->
                v.getRecord().addRoomRecord(roomRecord));

        //加入数据库保存列表
        GameManager.getInstance().getSaveUser2DB().addAll(room.getUserMap().values());


    }
    /**
     * 开始打牌
     * @param dizhu
     */
    private void startPlay(long dizhu){
        this.canQiangUser = -1;
        this.canJiaoUser = -1;
        this.dizhu = dizhu;
        this.step = STEP_PLAY;
        this.playTurn = dizhu;



        //选定地主
        Map<String, Long> rs = new HashMap<>();
        rs.put("userId",dizhu);
        Player.sendMsg2Player(new ResponseVo("gameService","chooseDizhu",rs),users);

        //把底牌加到地主身上
        PlayerCardInfo playerCardInfo = playerCardInfos.get(dizhu);
        if (playerCardInfo != null) {
            playerCardInfo.cards.addAll(tableCards);
            Player.sendMsg2Player(new ResponseVo("gameService","showTableCard",tableCards),dizhu);
        }

    }


    /**
     * 抢地主
     * @param player
     * @param isQiang
     * @return
     */
    public int qiangDizhu(Player player,boolean isQiang) {
        logger.info(player.getUser().getAccount() +"  抢地主 "+isQiang);

        if(player.getUserId() != canQiangUser){
            return ErrorCode.CAN_NOT_QIANG_TURN;
        }
        this.chooseQiangSet.add(player.getUserId());
        int jiaoIndex = chooseJiaoSet.size();

        PlayerCardInfo playerCardInfo = playerCardInfos.get(player.getUserId());
        playerCardInfo.setQiang(true);
        if (jiaoIndex == 1) {
            handleQiang1(player.getUserId(),isQiang);
        } else if (jiaoIndex == 2) {
            handleQiang2(player.getUserId(), isQiang);
        }

        player.sendMsg(new ResponseVo("gameService","qiangDizhu",0));
        return 0;
    }


    /**
     * 处理第一个人叫的情况
     * @param qiangUser
     * @param isQiang
     */
    private void handleQiang1(long qiangUser,boolean isQiang){
        logger.info("第一个人叫");
        if (isQiang) {
            this.qiangUser = qiangUser;
            if (chooseQiangSet.size() == 1) {
                canQiangUser = nextTurnId(qiangUser);
                noticeCanQiang(canQiangUser);
            } else if(chooseQiangSet.size() ==2) {
                startPlay(jiaoUser);
            }


        } else {//不抢
            if (chooseQiangSet.size() == 1) {
                canQiangUser = nextTurnId(qiangUser);
                noticeCanQiang(canQiangUser);
            } else if(chooseQiangSet.size() ==2) {
//                long dizhu = this.qiangUser == 0?jiaoUser:qiangUser;
                startPlay(jiaoUser);
            }

        }
    }

    /**
     * 处理第二个人叫的情况
     * @param qiangUser
     * @param isQiang
     */
    private void handleQiang2(long qiangUser,boolean isQiang){
        logger.info("第二个人叫");
        if (isQiang) {
            this.qiangUser = qiangUser;
            if (chooseQiangSet.size() == 1) {
                canQiangUser = nextTurnId(qiangUser);
                noticeCanQiang(canQiangUser);
            } else if(chooseQiangSet.size() ==2) {
                startPlay(jiaoUser);
            }


        } else {//不抢

//            long dizhu = this.qiangUser == 0?jiaoUser:qiangUser;
            startPlay(jiaoUser);
        }
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


    public List<Integer> getCards() {
        return cards;
    }

    public GameDouDiZhu setCards(List<Integer> cards) {
        this.cards = cards;
        return this;
    }

    public List<Integer> getDisCards() {
        return disCards;
    }

    public GameDouDiZhu setDisCards(List<Integer> disCards) {
        this.disCards = disCards;
        return this;
    }

    public List<Integer> getTableCards() {
        return tableCards;
    }

    public GameDouDiZhu setTableCards(List<Integer> tableCards) {
        this.tableCards = tableCards;
        return this;
    }

    public Map<Long, PlayerCardInfo> getPlayerCardInfos() {
        return playerCardInfos;
    }

    public GameDouDiZhu setPlayerCardInfos(Map<Long, PlayerCardInfo> playerCardInfos) {
        this.playerCardInfos = playerCardInfos;
        return this;
    }

    public List<Long> getUsers() {
        return users;
    }

    public GameDouDiZhu setUsers(List<Long> users) {
        this.users = users;
        return this;
    }

    public Random getRand() {
        return rand;
    }

    public GameDouDiZhu setRand(Random rand) {
        this.rand = rand;
        return this;
    }

    public long getDizhu() {
        return dizhu;
    }

    public GameDouDiZhu setDizhu(long dizhu) {
        this.dizhu = dizhu;
        return this;
    }

    public Set<Long> getChooseJiaoSet() {
        return chooseJiaoSet;
    }

    public GameDouDiZhu setChooseJiaoSet(Set<Long> chooseJiaoSet) {
        this.chooseJiaoSet = chooseJiaoSet;
        return this;
    }

    public Set<Long> getChooseQiangSet() {
        return chooseQiangSet;
    }

    public GameDouDiZhu setChooseQiangSet(Set<Long> chooseQiangSet) {
        this.chooseQiangSet = chooseQiangSet;
        return this;
    }

    public Set<Long> getBujiaoSet() {
        return bujiaoSet;
    }

    public GameDouDiZhu setBujiaoSet(Set<Long> bujiaoSet) {
        this.bujiaoSet = bujiaoSet;
        return this;
    }




    public int getLasttype() {
        return lasttype;
    }

    public GameDouDiZhu setLasttype(int lasttype) {
        this.lasttype = lasttype;
        return this;
    }

    public long getCanJiaoUser() {
        return canJiaoUser;
    }

    public GameDouDiZhu setCanJiaoUser(long canJiaoUser) {
        this.canJiaoUser = canJiaoUser;
        return this;
    }

    public long getCanQiangUser() {
        return canQiangUser;
    }

    public GameDouDiZhu setCanQiangUser(long canQiangUser) {
        this.canQiangUser = canQiangUser;
        return this;
    }

    public long getJiaoUser() {
        return jiaoUser;
    }

    public GameDouDiZhu setJiaoUser(long jiaoUser) {
        this.jiaoUser = jiaoUser;
        return this;
    }

    public long getQiangUser() {
        return qiangUser;
    }

    public GameDouDiZhu setQiangUser(long qiangUser) {
        this.qiangUser = qiangUser;
        return this;
    }

    public long getPlayTurn() {
        return playTurn;
    }

    public GameDouDiZhu setPlayTurn(long playTurn) {
        this.playTurn = playTurn;
        return this;
    }

    public CardStruct getLastCardStruct() {
        return lastCardStruct;
    }

    public GameDouDiZhu setLastCardStruct(CardStruct lastCardStruct) {
        this.lastCardStruct = lastCardStruct;
        return this;
    }

    public int getStep() {
        return step;
    }

    public GameDouDiZhu setStep(int step) {
        this.step = step;
        return this;
    }

    public int getMultiple() {
        return multiple;
    }

    public GameDouDiZhu setMultiple(int multiple) {
        this.multiple = multiple;
        return this;
    }
}
