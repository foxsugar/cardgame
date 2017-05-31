package com.code.server.cardgame.playdice;

import com.code.server.cardgame.core.Game;
import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.core.Room;
import com.code.server.cardgame.core.tiandakeng.GameTianDaKeng;
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

    private static final Logger logger = LoggerFactory.getLogger(GameTianDaKeng.class);

    protected Map<Long,Integer> gameuserStatus = new HashMap<>();

    private long currentTurn;//当前操作人

    protected Room room;//房间

    public void startGame(List<Long> users,Room room){
        this.room = room;
        init(users);
    }
    public void init(List<Long> users){

    }


    /**
     * 下注
     * @param player
     * @return
     */
    public int bet(Player player,int chip){

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
     * 通知自己人牌
     * @param userId
     */
    private void noticeDealevery(long userId, List myselfCards, Map everyknowCards, long dealFirst){
        Map<String, Object> result = new HashMap<>();
        result.put("userId",userId);
        result.put("myselfCards",myselfCards);
        result.put("everyknowCards",everyknowCards);
        result.put("dealFirst",dealFirst);
        ResponseVo vo = new ResponseVo("gameService","dealevery",result);
        Player.sendMsg2Player(vo,userId);
    }


}
