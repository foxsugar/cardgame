package com.code.server.cardgame.response;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunxianping on 2017/3/30.
 */
public class GameFinalResult {
    private List<UserInfo> userInfos = new ArrayList<>();


    public static class UserInfo{
        private long userId;
        private double score;

        public UserInfo(long userId, double score) {
            this.userId = userId;
            this.score = score;
        }

        public UserInfo() {
        }
    }

    public List<UserInfo> getUserInfos() {
        return userInfos;
    }

    public GameFinalResult setUserInfos(List<UserInfo> userInfos) {
        this.userInfos = userInfos;
        return this;
    }
}
