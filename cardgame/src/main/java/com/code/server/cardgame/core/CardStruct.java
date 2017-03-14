package com.code.server.cardgame.core;

import java.util.List;
import java.util.Map;

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
    private static final int type_四带二 = 12;

    List<Integer> cards;
    int type;
    int dan;
    List<Integer> dui; //对
    List<Integer> san;  //三
    List<Integer> si;   //四
    List<Integer> zha;  //炸
    List<Integer> feiji; //飞机
    List<Integer> shun; //顺
    List<Integer> liandui; //连对
    List<Map<Integer,Integer>> sandaidan; //三带一   第一Integer 存3张一样，第二Integer 存1张
    List<Map<Integer,List<Integer>>> sandaier; //三带二  第一Integer 存3张一样，第二Lsit 存2张
    List<Map<Integer,List<Integer>>> sidaier; //四带二   第一Integer 存4张一样，第二Lsit 存2张
    List<Map<Integer,Integer>> feiji_chibang;//飞机带翅膀  第一Integer 存3张一样，第二Integer 存1张
}