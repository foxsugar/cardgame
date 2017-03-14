package com.code.server.cardgame.core;

import java.util.List;

/**
 * Created by sunxianping on 2017/3/14.
 */
public class CardStruct {
    private static final int type_单 = 1;
    private static final int type_对 = 2;
    private static final int type_三 = 3;
    private static final int type_三带单 = 4;
    private static final int type_三带对 = 5;
    private static final int type_顺 = 6;
    private static final int type_连对 = 7;
    private static final int type_飞机 = 8;
    private static final int type_飞机带翅膀 = 9;
    private static final int type_炸 = 10;
    private static final int type_火箭 =11;

    List<Integer> cards;
    int type;
    int dan;
    List<Integer> dui;
    List<Integer> san;
    List<Integer> si;
    List<Integer> zha;
    List<Integer> feiji;
    List<Integer> shun;
    List<Integer>

}
