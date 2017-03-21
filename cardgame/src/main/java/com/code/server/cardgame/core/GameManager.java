package com.code.server.cardgame.core;

import com.code.server.cardgame.core.room.Room;
import com.code.server.cardgame.core.room.RoomDouDiZhu;
import com.code.server.cardgame.utils.DbUtils;
import com.code.server.cardgame.utils.IdWorker;
import com.code.server.db.model.Constant;
import com.code.server.db.model.ServerInfo;
import com.code.server.db.model.User;
import com.code.server.gamedata.UserVo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.apache.commons.collections.map.HashedMap;

import java.util.*;

/**
 * Created by sun on 2015/8/26.
 */
public class GameManager {

    private static GameManager instance;
    public static AttributeKey<Long> attributeKey = AttributeKey.newInstance("userId");

    public int serverId;
    public Map<Long, Player> players = new HashMap<>();
    public Map<Long,String> id_nameMap = new HashMap<>();
    public Map<String, Long> name_idMap = new HashMap<>();
    public Map<String, Long> openId_uid = new HashMap<>();
    public Map<Long, String> uid_openId = new HashMap<>();
    public Map<Long, String> userRoom = new HashMap<>();
    public Map<String, RoomDouDiZhu> rooms = new HashMap<>();
    public ServerInfo serverInfo;
    public Constant constant;
    public Map<Long, Player> kickUser = new HashMap<>();


    public Map<Long, User> users = new HashMap<>();

    private IdWorker idWorker;







    public static UserVo getUserVo(User user){
        UserVo vo = new UserVo();


        vo.setId(user.getUserId());
        vo.setIpConfig(user.getIpConfig());
        vo.setAccount(user.getAccount());
        vo.setImage(user.getImage());
        vo.setMarquee(GameManager.getInstance().constant.getMarquee());
        vo.setSex(user.getSex());
        vo.setOpenId(user.getOpenId());
        vo.setMoney(user.getMoney());
        vo.setVip(user.getVip());
        vo.setUsername(user.getUsername());

        String room = GameManager.getInstance().userRoom.get(user.getUserId());
        if (room!=null && GameManager.getInstance().rooms.containsKey(room)) {
            vo.setRoomId(room);
        } else {
            vo.setRoomId("0");
        }
        return vo;
    }



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
        player.getCtx().channel().attr(attributeKey).setIfAbsent(player.getUserId());

    }

    public void removePlayer(Player player) {
        this.players.remove(player.getUserId());
        this.name_idMap.remove(player.getUser().getAccount());
        this.id_nameMap.remove(player.getUserId());
        this.uid_openId.remove(player.getUserId());
        this.openId_uid.remove(player.getUser().getOpenId());
        player.getCtx().channel().attr(attributeKey).setIfAbsent(-1L);
    }

    public Room getRoomByUser(long userId) {
        String roomId = userRoom.get(userId);
        if (roomId == null) {
            return null;
        }
        return rooms.get(roomId);
    }

    public static Player getPlayerByCtx(ChannelHandlerContext ctx) {
        if (ctx.channel().attr(attributeKey).get() != null) {
            long uid = ctx.channel().attr(attributeKey).get();
            Player player = GameManager.getInstance().players.get(uid);
            if (player != null) {
                player.setLastSendMsgTime(System.currentTimeMillis());
                GameManager.getInstance().getKickUser().remove(player.getUserId());
            }
            return player;
        }
        return null;
    }

    public Map<Long, String> getUserRoom() {
        return userRoom;
    }

    public GameManager setUserRoom(Map<Long, String> userRoom) {
        this.userRoom = userRoom;
        return this;
    }

    public Map<Long, Player> getPlayers() {
        return players;
    }

    public Map<Long, User> getUsers() {
        return users;
    }

    public void setUsers(Map<Long, User> users) {
        this.users = users;
    }

    public Map<Long, Player> getKickUser() {
        return kickUser;
    }

    public GameManager setKickUser(Map<Long, Player> kickUser) {
        this.kickUser = kickUser;
        return this;
    }
}
