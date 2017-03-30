package com.code.server.rpc.server;

import com.code.server.rpc.idl.AdminRPC;
import com.code.server.rpc.idl.GameRPC;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;

/**
 * Created by sunxianping on 2017/3/30.
 */
public class AdminRpcServer {

    public static TServer StartServer(int port, AdminRPC.AsyncIface iface) throws TTransportException {
        TProcessor tprocessor = new AdminRPC.AsyncProcessor<>(iface);
        // 传输通道 - 非阻塞方式
        TNonblockingServerSocket serverTransport = null;
        serverTransport = new TNonblockingServerSocket(port);
        //多线程半同步半异步
        TThreadedSelectorServer.Args tArgs = new TThreadedSelectorServer.Args(serverTransport);
        tArgs.processor(tprocessor);
        tArgs.transportFactory(new TFramedTransport.Factory());
        //二进制协议
        tArgs.protocolFactory(new TBinaryProtocol.Factory());
        // 多线程半同步半异步的服务模型
        TServer server = new TThreadedSelectorServer(tArgs);
        server.serve(); // 启动服务
        return server;
    }
}
