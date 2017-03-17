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
    private static final int type_四带二 = 10;
    private static final int type_炸 =11;
    private static final int type_火箭 = 12;

    int outCard = 0;//默认是出牌  0   ，1是不能出牌
    long Userid;
    List<Integer> cards;
    int type;
    List<Integer> dan;
    List<Integer> dui; //对
    List<Integer> san;  //三
    List<Integer> si;   //四
    List<Integer> zha;  //炸
    List<Integer> feiji; //飞机
    List<Integer> shun; //顺
    List<Integer> liandui; //连对
    List<Integer> sandaidan; //三带一
    List<Integer> sandaidui; //三带二
    List<Integer> sidaier; //四带二
    List<Integer> feiji_chibang;//飞机带翅膀
    List<Integer> huojian; //火箭

    public  List<Integer> getByTypeList(int type){
        if(type == 1){
            return dan;
        }else if(type == 2){
            return dui;
        }else if(type == 3){
            return san;
        }else if(type == 4){
            return sandaidan;
        }else if(type == 5){
            return sandaidui;
        }else if(type == 6){
            return shun;
        }else if(type == 7){
            return liandui;
        }else if(type == 8){
            return feiji;
        }else if(type == 9){
            return feiji_chibang;
        }else if(type == 10){
            return sidaier;
        }else if(type == 11){
            return zha;
        }else if(type == 12){
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