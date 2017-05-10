package com.code.server.cardgame.core.game;

import com.code.server.cardgame.core.*;
import com.code.server.cardgame.core.room.Room;
import com.code.server.cardgame.response.*;
import com.code.server.db.model.Record;
import com.code.server.db.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by sunxianping on 2017/3/13.
 */
public class GameDouDiZhuLinFen extends GameDouDiZhu{
    private static final Logger logger = LoggerFactory.getLogger(GameDouDiZhuLinFen.class);

    protected int initCardNum = 16;



    /**
     * 洗牌
     */
    @Override
    protected void shuffle(){
        for(int i=1;i<=54;i++){
            cards.add(i);
        }
        //去掉两张2
        cards.remove((Integer)7);
        cards.remove((Integer)5);
        Collections.shuffle(cards);
    }




    /**
     * 叫地主
     * @param player
     * @param isJiao
     * @return
     */
    @Override
    public int jiaoDizhu(Player player,boolean isJiao,int score){

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
                sendResult(true,false);
                room.clearReadyStatus(true);
                sendFinalResult();
            } else {
                long nextJiao = nextTurnId(player.getUserId());
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

        Map<String, Object> rs = new HashMap<>();
        rs.put("userId", player.getUserId());
        rs.put("isJiao", isJiao);
        Player.sendMsg2Player("gameService","jiaoResponse",rs,users);

        player.sendMsg(new ResponseVo("gameService","jiaoDizhu",0));
        return 0;
    }

    @Override
    protected void compute(boolean isDizhuWin){

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
        playerCardInfo.setQiang(isQiang);
        if (jiaoIndex == 1) {
            handleQiang1(player.getUserId(),isQiang);
        } else if (jiaoIndex == 2) {
            handleQiang2(player.getUserId(), isQiang);
        }

        Map<String, Object> rs = new HashMap<>();
        rs.put("userId", player.getUserId());
        rs.put("isQiang", isQiang);
        Player.sendMsg2Player("gameService","qiangResponse",rs,users);

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
        }
        handleQiangNotice();
    }

    /**
     * 处理第二个人叫的情况
     * @param qiangUser
     * @param isQiang
     */
    private void handleQiang2(long qiangUser,boolean isQiang){
        logger.info("第二个人叫");
        if (isQiang) {
            handleQiangNotice();
        } else {//不抢
            startPlay(jiaoUser);
        }
    }


    private void handleQiangNotice(){
        if (chooseQiangSet.size() == 1) {
            canQiangUser = nextTurnId(qiangUser);
            noticeCanQiang(canQiangUser);
        } else if(chooseQiangSet.size() ==2) {
            startPlay(jiaoUser);
        }
    }

    protected void startPlay(long dizhu){
        this.canQiangUser = -1;
        this.canJiaoUser = -1;
        this.dizhu = dizhu;
        this.step = STEP_PLAY;
        this.playTurn = dizhu;
        //选定地主
        pushChooseDizhu();

        //把底牌加到地主身上
        PlayerCardInfo playerCardInfo = playerCardInfos.get(dizhu);
        if (playerCardInfo != null) {
            playerCardInfo.cards.addAll(tableCards);
            Player.sendMsg2Player(new ResponseVo("gameService","showTableCard",tableCards),dizhu);
        }

    }

}
