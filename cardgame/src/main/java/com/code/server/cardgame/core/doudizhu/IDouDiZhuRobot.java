package com.code.server.cardgame.core.doudizhu;

import com.code.server.cardgame.robot.IRobot;

/**
 * Created by sunxianping on 2017/5/16.
 */
public interface IDouDiZhuRobot extends IRobot{


   void jiaoDizhu(GameDouDiZhu game);

   void qiangDizhu(GameDouDiZhu game);

   void play(GameDouDiZhu game);

   void pass(GameDouDiZhu game);
}
