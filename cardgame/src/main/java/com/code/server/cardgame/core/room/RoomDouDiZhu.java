package com.code.server.cardgame.core.room;

import com.code.server.cardgame.config.ServerConfig;
import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.core.game.Game;
import com.code.server.cardgame.core.game.GameDouDiZhu;
import com.code.server.cardgame.response.ErrorCode;
import com.code.server.cardgame.response.ResponseVo;
import com.code.server.cardgame.response.RoomTianDaKengVo;
import com.code.server.cardgame.response.RoomVo;
import com.code.server.cardgame.rpc.RpcManager;
import com.code.server.cardgame.utils.SpringUtil;
import com.code.server.cardgame.utils.ThreadPool;
import com.code.server.db.model.User;
import com.code.server.rpc.idl.Rebate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunxianping on 2017/3/13.
 */
public class RoomDouDiZhu extends Room{


    private ServerConfig serverConfig = SpringUtil.getBean(ServerConfig.class);
    public static final int STATUS_JOIN = 0;
    public static final int STATUS_READY = 1;
    public static final int STATUS_IN_GAME = 2;
    public static final int STATUS_DISSOLUTION = 3;
    public static final int STATUS_AGREE_DISSOLUTION = 4;

    public static final long FIVE_MIN = 1000L * 60 * 5;
    public static final int PERSONNUM = 3;

    protected boolean isCanDissloution = false;


    protected Game getGameInstance(){
        return new GameDouDiZhu();
    }

    public static int createRoom(Player player, int gameNumber, int multiple){
        if(GameManager.getInstance().userRoom.containsKey(player.getUserId())){
            return ErrorCode.CANNOT_CREATE_ROOM_ROLE_HAS_IN_ROOM;
        }
        int needMoney = getNeedMoney(gameNumber);
        if (player.getUser().getMoney() < needMoney) {
            return ErrorCode.CANNOT_CREATE_ROOM_MONEY;
        }
        RoomDouDiZhu room = new RoomDouDiZhu();
        room.personNumber = PERSONNUM;

        room.roomId = getRoomIdStr(genRoomId());
        room.createUser = player.getUserId();
        room.init(gameNumber,multiple);
        //房间加入列表
        room.roomAddUser(player);
        GameManager.getInstance().rooms.put(room.roomId, room);

        player.sendMsg(new ResponseVo("roomService","createRoom",new RoomVo(room,player)));

        return 0;
    }

    public void spendMoney() {
        User user = userMap.get(this.createUser);
        if (user != null) {
            user.setMoney(user.getMoney() - createNeedMoney);
            GameManager.getInstance().getSaveUser2DB().add(user);

            ThreadPool.getInstance().executor.execute(()->{
                List<Rebate> list = new ArrayList<>();
                list.add(getRebate(user, createNeedMoney));
                RpcManager.getInstance().sendRpcRebat(list);
            });
        }
    }

    private Rebate getRebate(User user,int num){
        return new Rebate().setId(GameManager.getInstance().nextId())
                .setUserId(user.getUserId())
                .setRefereeId(user.getReferee())
                .setTime(System.currentTimeMillis())
                .setRebateNum(num)
                .setIsHasReferee(user.getReferee()!=0);

    }



}
