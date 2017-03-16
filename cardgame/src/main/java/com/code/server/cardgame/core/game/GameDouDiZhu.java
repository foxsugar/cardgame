package com.code.server.cardgame.core.game;

import com.code.server.cardgame.core.CardStruct;
import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.core.PlayerCardInfo;
import com.code.server.cardgame.response.ErrorCode;
import com.code.server.cardgame.response.ResponseVo;

import java.util.*;

/**
 * Created by sunxianping on 2017/3/13.
 */
public class GameDouDiZhu extends Game{
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
    private long canJiaoUser;//可以叫地主的人
    private long canQiangUser;//可以抢地主的人
    private long jiaoUser;//叫的人
    private long qiangUser;//抢的人

    private long playTurn;//该出牌的人

    protected CardStruct lastCardStruct;

    private int step;//步骤


    public void startGame(List<Long> users,long dizhuUser){
        init(users,dizhuUser);
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

    protected void play(Player player){
        PlayerCardInfo playerCardInfo = playerCardInfos.get(player.getUserId());
        playerCardInfo.checkPlayCard(lastCardStruct);
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

    }

    /**
     * 叫地主
     * @param player
     * @param isJiao
     * @return
     */
    public int jiaoDizhu(Player player,boolean isJiao){

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

            } else {
                long nextJiao = nextTurnId(player.getUserId());
                canJiaoUser = nextJiao;
                noticeCanJiao(nextJiao);
            }
        } else {//叫了 开始抢
            jiaoUser = player.getUserId();
            //第三个人叫的 直接开始游戏
            if (chooseJiaoSet.size() >= users.size()) {

            } else {

                step = STEP_QIANG_DIZHU;
                long nextId = nextTurnId(player.getUserId());
                noticeCanQiang(nextId);
            }

        }

        player.sendMsg(new ResponseVo("gameService","jiaoDizhu",0));
        return 0;
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
        if(player.getUserId() != canQiangUser){
            return ErrorCode.CAN_NOT_QIANG_TURN;
        }
        this.chooseJiaoSet.add(player.getUserId());
        int jiaoIndex = chooseJiaoSet.size();

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
        if (isQiang) {
            this.qiangUser = qiangUser;
            if (chooseJiaoSet.size() == 1) {
                canQiangUser = nextTurnId(qiangUser);
                noticeCanQiang(canQiangUser);
            } else if(chooseJiaoSet.size() ==2) {
                startPlay(qiangUser);
            }


        } else {//不抢
            if (chooseJiaoSet.size() == 1) {
                canQiangUser = nextTurnId(qiangUser);
                noticeCanQiang(canQiangUser);
            } else if(chooseJiaoSet.size() ==2) {
                long dizhu = this.qiangUser == 0?jiaoUser:qiangUser;
                startPlay(dizhu);
            }

        }
    }

    /**
     * 处理第二个人叫的情况
     * @param qiangUser
     * @param isQiang
     */
    private void handleQiang2(long qiangUser,boolean isQiang){
        if (isQiang) {
            this.qiangUser = qiangUser;
            if (chooseJiaoSet.size() == 1) {
                canQiangUser = nextTurnId(qiangUser);
                noticeCanQiang(canQiangUser);
            } else if(chooseJiaoSet.size() ==2) {
                startPlay(qiangUser);
            }


        } else {//不抢

            long dizhu = this.qiangUser == 0?jiaoUser:qiangUser;
            startPlay(dizhu);
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





}
