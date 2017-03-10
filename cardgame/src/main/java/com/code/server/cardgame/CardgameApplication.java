package com.code.server.cardgame;

import com.code.server.cardgame.bootstarp.SocketServer;
import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.handler.GameProcessor;
import com.code.server.cardgame.utils.SpringUtil;
import com.code.server.db.Service.ServerService;
import com.code.server.db.model.ServerInfo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages={"com.code.server.*"})
public class CardgameApplication {

	public static void main(String[] args) {
		SpringApplication.run(CardgameApplication.class, args);
		init();
		new Thread(new SocketServer()).start();
		new Thread(GameProcessor.getInstance()).start();
	}

	public static void init(){
		//初始化服务器信息
		ServerService serverService = SpringUtil.getBean(ServerService.class);
		ServerInfo serverInfo = serverService.getAllServerInfo().get(0);
		GameManager.getInstance().serverInfo = serverInfo;
	}
}
