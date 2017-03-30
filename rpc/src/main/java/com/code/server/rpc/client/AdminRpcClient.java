package com.code.server.rpc.client;

import com.code.server.rpc.idl.AdminRPC;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import static com.code.server.rpc.client.TransportManager.getTransport;

/**
 * Created by sunxianping on 2017/3/30.
 */
public class AdminRpcClient {


    public static AdminRPC.Client getAClient(String host, int port) throws TTransportException {
        TTransport transport = getTransport(host, port);
        TProtocol protocol = new TBinaryProtocol(transport);
        AdminRPC.Client client = new AdminRPC.Client(protocol);
        return client;
    }

    public static AdminRPC.Client getAClient(TTransport transport) throws TTransportException {
        TProtocol protocol = new TBinaryProtocol(transport);
        AdminRPC.Client client = new AdminRPC.Client(protocol);
        return client;
    }

}
