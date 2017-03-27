package com.code.server.cardgame.core.room;

import org.apache.log4j.Logger;

/**
 * Created by sunxianping on 2017/3/13.
 */
public class RoomDouDiZhu extends Room{


    private static final Logger logger = Logger.getLogger("game");

    public static final int STATUS_JOIN = 0;
    public static final int STATUS_READY = 1;
    public static final int STATUS_IN_GAME = 2;
    public static final int STATUS_DISSOLUTION = 3;
    public static final int STATUS_AGREE_DISSOLUTION = 4;

    public static final long FIVE_MIN = 1000L * 60 * 5;
    public static final int PERSONNUM = 3;

    protected boolean isCanDissloution = false;






}
