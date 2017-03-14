package com.code.server.cardgame.core;

import com.code.server.cardgame.core.room.Room;
import com.code.server.cardgame.utils.IdWorker;
import com.code.server.db.model.Constant;
import com.code.server.db.model.ServerInfo;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.collections.map.HashedMap;

import java.util.*;

/**
 * Created by sun on 2015/8/26.
 */
public class GameManager {

    public static GameManager instance;

    public int serverId;
    public Map<Long, Player> players = new HashMap<>();
    public Map<Long, ChannelHandlerContext> ctxs = new HashMap<>();
    public Map<Long,String> id_nameMap = new HashMap<>();
    public Map<String, Long> name_idMap = new HashMap<>();
    public Map<String, Long> openId_uid = new HashedMap();
    public Map<Long, String> uid_openId = new HashedMap();
    public Map<Long, String> userRoom = new HashedMap();
    public Map<String, Room> rooms = new HashedMap();
    public Set<ChannelHandlerContext> ctxSet = new HashSet<>();
    public ServerInfo serverInfo;
    public Constant constant;


    private IdWorker idWorker;











    public long nextId(){
        if (idWorker == null) {
            idWorker = new IdWorker(serverId, 1);
        }
        return idWorker.nextId();
    }


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
        this.uid_openId.put(player.getUserId(), player.getUser().getOpenId());
        this.openId_uid.put(player.getUser().getOpenId(), player.getUserId());
        player.getCtx().channel().attr(MsgDispatch.attributeKey).setIfAbsent(player.getUserId());

    }

    public void removePlayer(Player player) {
        this.players.remove(player.getUserId());
        this.name_idMap.remove(player.getUser().getAccount());
        this.id_nameMap.remove(player.getUserId());
        this.uid_openId.remove(player.getUserId());
        this.openId_uid.remove(player.getUser().getOpenId());
        player.getCtx().channel().attr(MsgDispatch.attributeKey).setIfAbsent(-1L);
    }


    public Map<Long, String> getUserRoom() {
        return userRoom;
    }

    public GameManager setUserRoom(Map<Long, String> userRoom) {
        this.userRoom = userRoom;
        return this;
    }
}
