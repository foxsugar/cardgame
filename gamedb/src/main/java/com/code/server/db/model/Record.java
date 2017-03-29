package com.code.server.db.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunxianping on 2017/3/24.
 */
public class Record {
    private static final int MAX_SIZE = 20;

    private List<RoomRecord> roomRecords = new ArrayList<>();

    public void addRoomRecord(RoomRecord roomRecord){
        this.roomRecords.add(roomRecord);
        if(roomRecords.size()>MAX_SIZE){
            this.roomRecords.remove(0);
        }
    }

    public static class RoomRecord{
        List<UserRecord> records = new ArrayList<>();

        public void addRecord(UserRecord userRecord){
            records.add(userRecord);
        }
    }

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

    public List<RoomRecord> getRoomRecords() {
        return roomRecords;
    }

    public Record setRoomRecords(List<RoomRecord> roomRecords) {
        this.roomRecords = roomRecords;
        return this;
    }
}
