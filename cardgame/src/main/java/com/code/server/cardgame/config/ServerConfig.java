package com.code.server.cardgame.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by sunxianping on 2017/3/17.
 */
@ConfigurationProperties(prefix = "serverConfig")
public class ServerConfig {

    private int serverId;
    private int dbSaveTime;
    private int port;
    private int kickTime;


    public int getServerId() {
        return serverId;
    }

    public ServerConfig setServerId(int serverId) {
        this.serverId = serverId;
        return this;
    }

    public int getDbSaveTime() {
        return dbSaveTime;
    }

    public ServerConfig setDbSaveTime(int dbSaveTime) {
        this.dbSaveTime = dbSaveTime;
        return this;
    }

    public int getPort() {
        return port;
    }

    public ServerConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public int getKickTime() {
        return kickTime;
    }

    public ServerConfig setKickTime(int kickTime) {
        this.kickTime = kickTime;
        return this;
    }
}