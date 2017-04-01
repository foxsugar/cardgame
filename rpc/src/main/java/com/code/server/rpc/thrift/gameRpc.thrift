namespace java com.code.server.rpc.idl

include "response.thrift"

struct User {
  1: i64 id = 0,
  2: string username,
  7: double money,
  13:double gold,

}

enum ChargeType{
      money = 1,
      gold = 2
}
struct Order{
    1:i64 userId,
    2:double num,
    3:i32 type,
    4:string token,
    5:i32 agentId,
    6:i64 id,
}

service GameRPC{
    //充值
    i32 charge(Order order),

   //获得用户信息
   User getUserInfo(i64 userId),

   //交易库存斗
   i32 exchange(Order order)
}

