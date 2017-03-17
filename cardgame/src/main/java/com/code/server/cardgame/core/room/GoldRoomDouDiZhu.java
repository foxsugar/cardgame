package com.code.server.cardgame.core.room;

import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.core.game.GameDouDiZhu;
import com.code.server.cardgame.encoding.Notice;
import com.code.server.cardgame.response.*;
import com.code.server.cardgame.timer.GameTimer;
import com.code.server.cardgame.timer.TimerNode;
import com.code.server.db.model.User;
import com.code.server.gamedata.UserVo;
import com.google.gson.Gson;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * Created by sunxianping on 2017/3/13.
 */
public class GoldRoomDouDiZhu extends RoomDouDiZhu{



    public static int joinRoom(Player player,int gameNumber,int multiple,double roomType){
        int result = GoldRoomPool.getInstance().addRoom(gameNumber,multiple,roomType,player);
        player.sendMsg(new ResponseVo("roomService","joinRoom",result));
        return 0;
    }

    public int quitRoom(Player player) {
        int result = GoldRoomPool.getInstance().quitRoom(this.roomType,player.getUserId());
        player.sendMsg(new ResponseVo("roomService","quitRoom",result));
        return 0;
    }



}
