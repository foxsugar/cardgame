package com.code.server.rpc.client;

import com.code.server.rpc.idl.GameRPC;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

/**
 * Created by sunxianping on 2017/3/29.
 */
public class GameRpcClient {

    private static GameRpcClient instance;

    public GameRpcClient() {

    }

    public static GameRpcClient getInstance() {
        if (instance == null) {
            instance = new GameRpcClient();
        }
        return instance;
    }


    public static TTransport getTransport(String host, int port) throws TTransportException {
        TTransport transport = new TFramedTransport(new TSocket(host, port));
        transport.open();
        return transport;

    }

    public static GameRPC.Client getAClient(String host, int port) throws TTransportException {
        TTransport transport = getTransport(host, port);
        TProtocol protocol = new TBinaryProtocol(transport);
        GameRPC.Client client = new GameRPC.Client(protocol);
        return client;
    }

    public static GameRPC.Client getAClient(TTransport transport) throws TTransportException {
        TProtocol protocol = new TBinaryProtocol(transport);
        GameRPC.Client client = new GameRPC.Client(protocol);
        return client;
    }



    public static void main(String [] args) {
        try {
            TTransport tTransport = GameRpcClient.getTransport("localhost", 9090);
            GameRPC.Client client = GameRpcClient.getAClient(tTransport);
            client.charge(1, 1);

            tTransport.close();
        } catch (TException x) {
            x.printStackTrace();
        }
    }
}
