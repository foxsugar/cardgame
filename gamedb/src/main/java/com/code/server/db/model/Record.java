package com.code.server.db.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunxianping on 2017/3/24.
 */
public class Record {

    private List<UserRecord> userRecords = new ArrayList<>();

    public static class UserRecord{
        public UserRecord(){
        }

        public UserRecord(long userId, String name, double score) {
            this.userId = userId;
            this.name = name;
            this.score = score;
        }

        private long userId;
        private String name;
        private double score;

        public long getUserId() {
            return userId;
        }

        public UserRecord setUserId(long userId) {
            this.userId = userId;
            return this;
        }

        public String getName() {
            return name;
        }

        public UserRecord setName(String name) {
            this.name = name;
            return this;
        }

        public double getScore() {
            return score;
        }

        public UserRecord setScore(double score) {
            this.score = score;
            return this;
        }
    }

    public List<UserRecord> getUserRecords() {
        return userRecords;
    }

    public Record setUserRecords(List<UserRecord> userRecords) {
        this.userRecords = userRecords;
        return this;
    }
}
