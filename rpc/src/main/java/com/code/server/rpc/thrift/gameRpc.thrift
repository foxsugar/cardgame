namespace java com.code.server.rpc.idl

struct Order{
    1:i64 userId,
    2:double money,
    3:i32 type,
    4:string token,
}

service GameRPC{
    i32 charge(Order order),
}