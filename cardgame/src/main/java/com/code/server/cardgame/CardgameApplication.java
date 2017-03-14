package com.code.server.cardgame;

import com.code.server.cardgame.bootstarp.SocketServer;
import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.handler.GameProcessor;
import com.code.server.cardgame.utils.ProperitesUtil;
import com.code.server.cardgame.utils.SpringUtil;
import com.code.server.db.Service.ConstantService;
import com.code.server.db.Service.ServerService;
import com.code.server.db.model.Constant;
import com.code.server.db.model.ServerInfo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Properties;

@SpringBootApplication(scanBasePackages={"com.code.server.*"})
public class CardgameApplication {

	public static void main(String[] args) {
		SpringApplication.run(CardgameApplication.class, args);
		init();
		new Thread(new SocketServer()).start();
		new Thread(GameProcessor.getInstance()).start();
	}

	public static void init(){

		Properties p = null;
		try {
			p = ProperitesUtil.loadProperties("application.properties");
			int serverId = Integer.valueOf(p.getProperty("gameServerId"));
			GameManager.getInstance().serverId = serverId;

		} catch (Exception e) {
			e.printStackTrace();
		}

		//初始化服务器信息
		ServerService serverService = SpringUtil.getBean(ServerService.class);
		ServerInfo serverInfo = serverService.getAllServerInfo().get(0);
		GameManager.getInstance().serverInfo = serverInfo;

		//常量数据
		ConstantService constantService = SpringUtil.getBean(ConstantService.class);
		Constant constant = constantService.constantDao.findOne(1L);
		GameManager.getInstance().constant = constant;

	}
}
