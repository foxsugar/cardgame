package com.code.server.cardgame;

import com.code.server.cardgame.bootstarp.SocketServer;
import com.code.server.cardgame.config.ServerConfig;
import com.code.server.cardgame.config.ServerState;
import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.handler.GameProcessor;
import com.code.server.cardgame.rpc.RpcManager;
import com.code.server.cardgame.utils.SaveUserTimerTask;
import com.code.server.cardgame.utils.SpringUtil;
import com.code.server.cardgame.utils.ThreadPool;
import com.code.server.db.Service.ConstantService;
import com.code.server.db.Service.ServerService;
import com.code.server.db.model.Constant;
import com.code.server.db.model.ServerInfo;
import org.apache.thrift.transport.TTransportException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.Date;
import java.util.Timer;

@SpringBootApplication(scanBasePackages={"com.code.server.*"})
@EnableConfigurationProperties({ServerConfig.class})
public class CardgameApplication {


	public static void main(String[] args) throws TTransportException {



		SpringApplication.run(CardgameApplication.class, args);
		init();
		ThreadPool.getInstance().executor.execute(new SocketServer());
		ThreadPool.getInstance().executor.execute(GameProcessor.getInstance());
		//定时保存User数据
        ServerConfig serverConfig = SpringUtil.getBean(ServerConfig.class);
		ThreadPool.getInstance().executor.execute(()->{
				Timer timer = new Timer();
				timer.schedule(new SaveUserTimerTask() , new Date(), serverConfig.getDbSaveTime());

		});

		//rpc服务
		RpcManager.getInstance().startGameRpcServer();
		RpcManager.getInstance().checkGameRpcServerWork();

		ServerState.isWork = true;
	}

	public static void init(){

		ServerConfig serverConfig = SpringUtil.getBean(ServerConfig.class);
		GameManager.getInstance().serverId = serverConfig.getServerId();

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
