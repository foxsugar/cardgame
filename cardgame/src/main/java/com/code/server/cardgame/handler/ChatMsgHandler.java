package com.code.server.cardgame.handler;


import com.code.server.cardgame.encoding.ChatUser;
import com.code.server.cardgame.encoding.Notice;
import com.code.server.cardgame.encoding.ResponseVo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.sf.json.JSONObject;

/**
 * Created by win7 on 2017/2/13.
 */
public class ChatMsgHandler extends ChannelInboundHandlerAdapter {


    public ChatMsgHandler(){
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        final JSONObject jSONObject = (JSONObject)msg;

        String service = jSONObject.getString("service");
        String method = jSONObject.getString("method");
        JSONObject params = jSONObject.getJSONObject("params");

        if ("newChatService".equals(service)) {
            switch (method) {
                case "login":
                    int userId = Integer.valueOf(params.getString("userId"));
                    ChatUser.getInstance().users.put(userId, ctx);
                    ResponseVo vo = new ResponseVo("newChatService", "login", "");
                    ctx.writeAndFlush(vo.toJsonObject());
                    break;
                case "sendMessage":
                    String userIdOfMsg = params.getString("userId");
                    String messageType = params.getString("messageType");
                    String message = params.getString("message");
                    String users = params.getString("users");

                    JSONObject result = new JSONObject();
                    Notice sendNotice = new Notice();
                    Notice acceptNotice = new Notice();
                    sendNotice.setMessage("send message success!");
                    sendNotice.setMessageType(messageType);
                    sendNotice.setSendUserId(userIdOfMsg);
                    acceptNotice.setMessage(message);
                    acceptNotice.setMessageType(messageType);
                    acceptNotice.setSendUserId(userIdOfMsg);

                    JSONObject noticeResult = new JSONObject();
                    noticeResult.put("service", "newChatService");
                    noticeResult.put("method", "acceptMessage");
                    noticeResult.put("params", acceptNotice.toJSONObject());
                    noticeResult.put("code", "0");

                    if (users != null && !users.isEmpty()) {
                        ChannelHandlerContext context = null;
                        for (String s : users.split(",")) {
                            context = ChatUser.getInstance().users.get(Integer.parseInt(s));
                            if(context!=null){
                                context.writeAndFlush(noticeResult);
                            }
                        }
                    }

                    result.put("service", "newChatService");
                    result.put("method", "sendMessage");
                    result.put("params", sendNotice.toJSONObject());
                    result.put("code", "0");
                    ctx.writeAndFlush(result);
                    break;
                case "sendMessageToOne":
                    String sendUserId = params.getString("sendUserId");
                    String acceptUserId = params.getString("acceptUserId");
                    String messageTypeToOne = params.getString("messageType");
                    String messageToOne = params.getString("message");
                    String usersToOne = params.getString("users");

                    JSONObject resultToOne = new JSONObject();

                    Notice sendNoticeToOne = new Notice();
                    Notice acceptNoticeToOne = new Notice();
                    sendNoticeToOne.setMessage("send message success!");
                    sendNoticeToOne.setMessageType(messageTypeToOne);
                    acceptNoticeToOne.setMessage(messageToOne);
                    acceptNoticeToOne.setMessageType(messageTypeToOne);
                    acceptNoticeToOne.setAcceptUserId(acceptUserId);
                    acceptNoticeToOne.setSendUserId(sendUserId);

                    JSONObject noticeResultToOne = new JSONObject();
                    noticeResultToOne.put("service", "newChatService");
                    noticeResultToOne.put("method", "acceptMessage");
                    noticeResultToOne.put("params", acceptNoticeToOne.toJSONObject());
                    noticeResultToOne.put("code", "0");

                    resultToOne.put("service", "newChatService");
                    resultToOne.put("method", "sendMessage");
                    resultToOne.put("params", sendNoticeToOne.toJSONObject());
                    resultToOne.put("code", "0");

                    if (usersToOne != null && !usersToOne.isEmpty()) {
                        ChannelHandlerContext context = null;
                        for (String s : usersToOne.split(",")) {
                            context = ChatUser.getInstance().users.get(Integer.parseInt(s));
                            if(context!=null){
                                context.writeAndFlush(noticeResultToOne);
                            }
                        }
                    }

                    break;
            }

        } else {
            super.channelRead(ctx, msg);
        }
    }
}
