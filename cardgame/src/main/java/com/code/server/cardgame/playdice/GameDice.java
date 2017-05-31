package com.code.server.cardgame.playdice;

import com.code.server.cardgame.core.Game;
import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.response.ErrorCodeTDK;
import com.code.server.cardgame.response.ResponseVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final Logger logger = LoggerFactory.getLogger(GameDice.class);

    /*

        下注		    21已下注   20未下注
        杀/不杀		41杀       40不杀
        摇筛子		50
    */
    protected Map<Long,Integer> gameUserStatus = new HashMap<>();

    protected Map<Long,Integer> gameUserScore = new HashMap<>();

    protected Map<Long,List<Integer>> allDiceNumber = new HashMap<>();//所有玩家点数

    private long currentTurn;//当前操作人

    protected RoomDice room;//房间

    public void startGame(List<Long> users,RoomDice room){
        this.room = room;
        if(room.getCurBanker()==null){
            room.setCurBanker(users.get(0));
        }
        init(users);
    }
    public void init(List<Long> users){
        //初始化玩家
        for(Long uid : users){
            PlayerCardInfoDice playerCardInfo = new PlayerCardInfoDice();
            playerCardInfo.userId = uid;
        }
        noticeWhoIsBanker(room);
    }


    /**
     * 下注
     * @param player
     * @return
     */
    public int bet(Player player,int chip){
        gameUserStatus.put(player.getUserId(),21);

        logger.info(player.getUser().getAccount() +"  下注: "+ chip);

        if (currentTurn != player.getUserId()) {
            return ErrorCodeTDK.CANNOT_BET;
        }

        if(!this.room.isLastDraw() && chip > MAX_BET_NUM){//下注错误
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
     * 不跟,不踢
     * @param player
     * @return
     */
    public int pass(Player player){
        return 0;
    }



    /**
     * 结算的时候分数的处理
     */
    public void dealScores(){

    }






    /**
     * 通知开局庄家是谁
     */
    private void noticeWhoIsBanker(RoomDice room){
        Map<String, Object> result = new HashMap<>();
        result.put("curBanker",room.getCurBanker());
        ResponseVo vo = new ResponseVo("gameService","noticeWhoIsBanker",result);
        Player.sendMsg2Player(vo,room.getUsers());
    }


    /**
     * 下个人
     * @param curId
     * @return
     */
    public long nextOne(List<Long> list,long curId) {
        int index = list.indexOf(curId);

        int nextId = index + 1;
        if (nextId >= list.size()) {
            nextId = 0;
        }
        return list.get(nextId);
    }
}
