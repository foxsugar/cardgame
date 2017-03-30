/**
 * The first thing to know about are types. The available types in Thrift are:
 *
 *  bool        Boolean, one byte
 *  i8 (byte)   Signed 8-bit integer
 *  i16         Signed 16-bit integer
 *  i32         Signed 32-bit integer
 *  i64         Signed 64-bit integer
 *  double      64-bit floating point value
 *  string      String
 *  binary      Blob (byte array)
 *  map<t1,t2>  Map from one type to another
 *  list<t1>    Ordered list of one type
 *  set<t1>     Set of unique elements of one type
 *
 * Did you also notice that Thrift supports C style comments?
 */


//namespace cpp tutorial

namespace java com.code.server.rpc.idl


//namespace php tutorial
//namespace perl tutorial




/**
 * You can define enums, which are just 32 bit integers. Values are optional
 * and start at 1 if not supplied, C style again.
 */
enum Operation {
  ADD = 1,
  SUBTRACT = 2,
  MULTIPLY = 3,
  DIVIDE = 4
}


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


service Charge{
    i32 calculate(1:i64 userId, 2:i32 money)

}