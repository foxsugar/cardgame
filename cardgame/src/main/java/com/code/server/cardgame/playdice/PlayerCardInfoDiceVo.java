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
public class PlayerCardInfoDiceVo {
    public long userId;

    public PlayerCardInfoDiceVo() {
    }

    public PlayerCardInfoDiceVo(PlayerCardInfoDice playerCardInfo, long uid) {
        this.userId = playerCardInfo.userId;
    }

    public PlayerCardInfoDiceVo(PlayerCardInfoDice playerCardInfo) {
        this.userId = playerCardInfo.userId;
    }

}
