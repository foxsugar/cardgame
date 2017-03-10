package com.code.server.cardgame.core;

import com.code.server.cardgame.Message.MessageHandler;
import com.code.server.db.Service.UserService;
import com.code.server.db.model.ServerInfo;
import com.code.server.db.model.User;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.collections.map.HashedMap;

import java.util.*;

/**
 * Created by sun on 2015/8/26.
 */
public class GameManager {

    public static GameManager instance;

    public Map<Long, Player> players = new HashMap<>();
    public Map<Long, ChannelHandlerContext> ctxs = new HashMap<>();
    public Map<Long,String> id_nameMap = new HashMap<>();
    public Map<String, Long> name_idMap = new HashMap<>();
    public Set<ChannelHandlerContext> ctxSet = new HashSet<>();
    public ServerInfo serverInfo;



    private Map<String, Object> roomLock = new HashedMap();



//    private HashMap<String, RoomInfo> rooms = new HashMap<>();

    private Map<Integer, User> users = new HashMap<>();

    private Map<Integer, String> userRoom = new HashMap<>();






    private GameManager() {

    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }


    public Player getPlayerByAccount(String account){
        if (name_idMap.containsKey(account)) {
            long uid = name_idMap.get(account);
            return players.get(uid);
        }
        return null;
    }

    public void addPlayer(Player player) {
        this.players.put(player.getUserId(), player);
        this.name_idMap.put(player.getUser().getAccount(), player.getUserId());
        this.id_nameMap.put(player.getUserId(), player.getUser().getAccount());
        player.getCtx().channel().attr(MessageHandler.attributeKey).setIfAbsent(player.getUserId());

    }

    public void removePlayer(Player player) {
        this.players.remove(player.getUserId());
        this.name_idMap.remove(player.getUser().getAccount());
        this.id_nameMap.remove(player.getUserId());
        player.getCtx().channel().attr(MessageHandler.attributeKey).setIfAbsent(-1L);
    }
//    public void sendMsg2Client(ChannelHandlerContext ctx, PB.S2CMessage msg) {
//        BinaryWebSocketFrame bwf = new BinaryWebSocketFrame();
//        int length = msg.getSerializedSize();
//        bwf.content().writeInt(length);
//        bwf.content().writeBytes(msg.toByteArray());
//        ctx.writeAndFlush(bwf);
//    }
}
