package com.code.server.cardgame.playdice;

import com.code.server.cardgame.response.ErrorCode;

/**
 * 项目名称：${project_name}
 * 类名称：${type_name}
 * 类描述：
 * 创建人：Clark
 * 创建时间：${date} ${time}
 * 修改人：Clark
 * 修改时间：${date} ${time}
 * 修改备注：
 *
 * @version 1.0
 */
public class ErrorCodeDice extends ErrorCode {

    public static final int MORE_BET = 400001;

    public static final int HAVE_BET = 400002;

    public static final int BANKER_NO_NEED_BET = 400003;

    public static final int NOT_IN_THIS_ROOM = 400004;

    public static final int CANNOT_ADD_REFUSE_ROOM = 400005;

    public static final int CANNOT_ROCK_NO_KILL = 400006;//没杀不能摇色子

    public static final int CANNOT_ROCK_HAVE_ROCK = 400007;//不能摇因为已经摇过了

}
