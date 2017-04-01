namespace java com.code.server.rpc.idl

include "response.thrift"

struct User {
  1: i64 id = 0,
  2: string username,
  3: string image,
  4: string seatId,
  5: string account,
  6: string ipConfig,
  7: double money,
  8: string roomId,
  9: i32 vip,
  10: string uuid,
  11: string openId,
  12: i32 sex,
  13: string marquee
}

struct Order{
    1:i64 userId,
    2:double money,
    3:i32 type,
    4:string token,
    5:i32 agentId,
}

service GameRPC{
    //充值
    i32 charge(Order order),

   //获得用户信息
   User getUserInfo(i64 userId),

   //交易库存斗
   i32 exchange(Order order)
}

