package com.code.server.cardgame.robot;

import com.code.server.cardgame.config.ServerConfig;
import com.code.server.cardgame.utils.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunxianping on 2017/5/16.
 */
public class RobotManager implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(RobotManager.class);

    private static RobotManager instance;

    private ServerConfig serverConfig = SpringUtil.getBean(ServerConfig.class);

    private RobotManager(){}

    public static RobotManager getInstance(){
        if (instance == null) {
            instance = new RobotManager();
        }
        return instance;
    }
    public List<IRobot> robots = new ArrayList<>();


    public void addRobot(IRobot robot){
        this.robots.add(robot);
    }


    @Override
    public void run() {
        while (true) {
            try {
                robots.forEach(IRobot::execute);
                //休眠
                Thread.sleep(serverConfig.getRobotExeCycle());
                //System.out.println("机器人执行");
            }catch (Exception e){
                e.printStackTrace();
                logger.error("机器人执行出现错误: " + e);

            }
        }
    }
}
