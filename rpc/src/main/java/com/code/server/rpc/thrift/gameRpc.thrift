namespace java com.code.server.rpc.idl


service GameRPC{
    i32 charge(1:i64 userId, 2:i32 money),
}