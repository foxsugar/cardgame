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
     * 出牌
     * @param player
     */
    @Override
    public int play(Player player,CardStruct cardStruct){
        PlayerCardInfo playerCardInfo = playerCardInfos.get(player.getUserId());
        //不可出牌
        if(!playerCardInfo.checkPlayCard(lastCardStruct,cardStruct,lasttype)){
            return ErrorCode.CAN_NOT_PLAY;
        }

        userPlayCount.add(player.getUserId());
        playerCardInfo.setPlayCount(playerCardInfo.getPlayCount() + 1);

        long nextUserCard = nextTurnId(cardStruct.getUserId()); //下一个出牌的人

        cardStruct.setNextUserId(nextUserCard);
        playTurn = nextUserCard;

        Player.sendMsg2Player(new ResponseVo("gameService","playResponse",cardStruct),this.users);
        lasttype = cardStruct.getType();//保存这次出牌的类型
        lastCardStruct = cardStruct;//保存这次出牌的牌型

        //删除牌
        playerCardInfo.cards.removeAll(cardStruct.getCards());

         if(zhaCount < room.getMultiple() || room.getMultiple() == -1){
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

            sendFinalResult();

        }
        player.sendMsg("gameService","play",0);
        return 0;
    }


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

}
