package com.code.server.cardgame.core;

import java.util.List;


/**
 * Created by sunxianping on 2017/3/14.
 */
public class CardStruct {

    protected static final int type_单 = 1;
    protected static final int type_对 = 2;
    protected static final int type_三 = 3;
    protected static final int type_三带单 = 4;
    protected static final int type_三带对 = 5;
    protected static final int type_顺 = 6;
    protected static final int type_连对 = 7;
    protected static final int type_飞机 = 8;
    protected static final int type_飞机带翅膀_单 = 9;
    protected static final int type_飞机带翅膀_对 = 10;
    protected static final int type_四带二 = 11;
    protected static final int type_炸 =12;
    protected static final int type_火箭 = 13;

    protected static final int type_飞机带翅膀 = 20;


    int outCard = 0;//默认是出牌  0
    long Userid;
    List<Integer> cards;
    int type;
    List<Integer> dan;// 单
    List<Integer> dui; //对
    List<Integer> san;  //三
    List<Integer> zha;  //炸
    List<Integer> feiji; //飞机
    List<Integer> shun; //顺
    List<Integer> liandui; //连对
    List<Integer> sandaidan; //三带一
    List<Integer> sandaidui; //三带二
    List<Integer> sidaier; //四带二
    List<Integer> feiji_chibang_dan;//飞机带翅膀_单
    List<Integer> feiji_chibang_dui;//飞机带翅膀_对
    List<Integer> huojian; //火箭

    public  List<Integer> getByTypeList(int type){
        if(type == type_单){
            return dan;
        }else if(type == type_对){
            return dui;
        }else if(type == type_三){
            return san;
        }else if(type == type_三带单){
            return sandaidan;
        }else if(type == type_三带对){
            return sandaidui;
        }else if(type == type_顺){
            return shun;
        }else if(type == type_连对){
            return liandui;
        }else if(type == type_飞机){
            return feiji;
        }else if(type == type_飞机带翅膀_单){
            return feiji_chibang_dan;
        }else if(type == type_飞机带翅膀_对){
            return feiji_chibang_dui;
        }else if(type == type_四带二){
            return sidaier;
        }else if(type == type_炸){
            return zha;
        }else if(type == type_火箭){
            return huojian;
        }else{
            return null;
        }
    }
    public int getType(){
        return type;
    }

    public int getOutCard (){
        return outCard;
    }
    public void setOutCard(int card){
        this.outCard = card;
    }
}