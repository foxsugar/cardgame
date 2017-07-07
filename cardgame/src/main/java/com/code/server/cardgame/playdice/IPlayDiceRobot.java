package com.code.server.cardgame.playdice;

import com.code.server.cardgame.core.Room;
import com.code.server.cardgame.robot.IRobot;

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
public interface IPlayDiceRobot extends IRobot{

    public void bet(GameDice game,Long userId);

    public void rock(GameDice game,Long userId);

    public void kill(GameDice game,Long userId);

    public void killAll(GameDice game,Long userId);

    public  void getReady(Room room,String userId);
}
