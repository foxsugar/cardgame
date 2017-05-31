package com.code.server.cardgame.playdice;

import com.code.server.cardgame.core.Room;

import java.util.*;

/**
 * 项目名称：${project_name}
 * 类名称：${type_name}
 * 类描述：
 * 创建人：Clark
 * 创建时间：${date} ${time}
 * 修改人：Clark
 * 修改时间：${date} ${time}
 * 修改备注：
 *
 * @version 1.0
 */
public class DiceNumberUtils {

   public static List<List<Integer>> list = new ArrayList<List<Integer>>();//所有的筛子点数

   public static List<List<Integer>> listEffective = new ArrayList<List<Integer>>();//有效的筛子点数

    public static List<List<Integer>> listInvalid = new ArrayList<List<Integer>>();// 无效的筛子点数

    public static List<Integer> special1 = new ArrayList<Integer>();//123

    public static List<Integer> special2 = new ArrayList<Integer>();//456

    static {
        for(int i = 1;i<=6;i++){
            for(int j = 1;j<=6;j++){
                for(int z = 1;z<=6;z++){
                    List<Integer> card = new ArrayList<Integer>();
                    card.add(i);
                    card.add(j);
                    card.add(z);
                    Collections.sort(card);
                    list.add(card);
                }
            }
        }

        for(int i = 1;i<=6;i++){
            for(int j = 1;j<=6;j++){
                if(i!=j){
                    List<Integer> card = new ArrayList<Integer>();
                    card.add(i);
                    card.add(i);
                    card.add(j);
                    Collections.sort(card);
                    listEffective.add(card);
                }
            }
            List<Integer> card = new ArrayList<Integer>();
            card.add(i);
            card.add(i);
            card.add(i);
            listEffective.add(card);
        }
        List<Integer> card = new ArrayList<Integer>();
        card.add(1);
        card.add(2);
        card.add(3);

        List<Integer> card1 = new ArrayList<Integer>();
        card1.add(4);
        card1.add(5);
        card1.add(6);

        listEffective.add(card);
        listEffective.add(card1);

        listInvalid.addAll(list);
        listInvalid.removeAll(listEffective);

        special1.add(1);
        special1.add(2);
        special1.add(3);
        special2.add(4);
        special2.add(5);
        special2.add(6);
    }

    /**
     * 摇筛子
     * @return
     */
    public static List<Integer> getPoints(){
        Random random = new Random();
        return list.get(random.nextInt(list.size()));
    }

    /**
     * 判断筛子是否有效
     * @param list
     * @return
     */
    public static boolean getIsEffective(List<Integer> list){
        return listEffective.contains(list);
    }

    /**
     * 比较筛子大小
     * 如果结果为1证明牌型点数相同
     * @param GameDice
     * @param user1
     * @param user2
     * @return
     */
    public static Long getMaxUser(GameDice GameDice,Long user1,Long user2){
       List<Integer> listUser1 = GameDice.allDiceNumber.get(user1);
       List<Integer> listUser2 = GameDice.allDiceNumber.get(user2);
       if(getCardScore(listUser1) > getCardScore(listUser2)){
                return user1;
       }else if(getCardScore(listUser1) < getCardScore(listUser2)){
                return user2;
       }else if(getCardScore(listUser1).intValue() == getCardScore(listUser2).intValue()){
                return new Long(1);
       }
       return null;
    }

    /**
     * 判断是否是通杀
     * @param list
     * @return
     */
    public static boolean getKill(List<Integer> list){
        return getCardScore(list) == 6;
    }

    /**
     * 判断是否是通杀
     * @param list
     * @return
     */
    public static boolean getCompensate(List<Integer> list){
        return getCardScore(list) == 1;
    }


    /**
     * 获取筛子分数
     * @param list
     * @return
     */
    public static Integer getCardScore(List<Integer> list){
        if(list.containsAll(special1)){
            return 1;
        }
        if(list.containsAll(special2)){
            return 6;
        }
        Map<Integer,Integer> map = new HashMap<Integer,Integer>();
        for(Integer a : list){
            if(map.containsKey(a)){
                map.put(a,map.get(a)+1);
            }else{
                map.put(a,1);
            }
        }
        for (Integer i:map.keySet()) {
            if(map.get(i)==1){
                return i;
            }else if(map.get(i)==3){
                return 6;//豹子所有的大小都一样
            }
        }
        return 0;
    }

}
