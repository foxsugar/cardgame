package com.code.server.cardgame.playdice;

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
public class ThreePlayerScore {

    private Long userId;

    private PlayerCardInfoDice PlayerCardInfoDice;

    private int one;

    private int two;

    private int three;

    public com.code.server.cardgame.playdice.PlayerCardInfoDice getPlayerCardInfoDice() {
        return PlayerCardInfoDice;
    }

    public void setPlayerCardInfoDice(com.code.server.cardgame.playdice.PlayerCardInfoDice playerCardInfoDice) {
        PlayerCardInfoDice = playerCardInfoDice;
    }

    public int getOne() {
        return one;
    }

    public void setOne(int one) {
        this.one = one;
    }

    public int getTwo() {
        return two;
    }

    public void setTwo(int two) {
        this.two = two;
    }

    public int getThree() {
        return three;
    }

    public void setThree(int three) {
        this.three = three;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
