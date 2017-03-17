package com.code.server.cardgame.bootstarp;

import com.code.server.cardgame.config.ServerConfig;
import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.handler.GameProcessor;
import com.code.server.cardgame.utils.*;
import com.code.server.cardgame.utils.ThreadPool;
import com.code.server.db.Service.UserService;
import com.code.server.db.model.User;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.util.*;

/**
 * Created by SunXianping on 2016/6/15 0015.
 */
public class SocketServer implements Runnable{

    static final boolean SSL = System.getProperty("ssl") != null;
    UserService userService = SpringUtil.getBean(UserService.class);
    ServerConfig serverConfig = SpringUtil.getBean(ServerConfig.class);


    private void start() throws Exception{
        int port = serverConfig.getPort();


        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
        } else {
            sslCtx = null;
        }

        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new SocketServerInitializer(sslCtx));

            // Start the server.
            ChannelFuture f = b.bind(port).sync();

            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void run() {
        try {
            start();
            Timer timer = new Timer();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    timer.schedule(new SaveUserTimerTask() , new Date(), 1000 * 60 * 5);
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

    }
}
