package com.yax.redisqueue.CustomEncoder;


import com.alibaba.fastjson.JSONObject;
import com.yax.redisqueue.messageModel.MessageModel;
import com.yax.redisqueue.messageModel.ResponseInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class ByteToStringDecode extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
            byte[] req = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(req);
            String body = new String(req, "utf-8");
            if(body.contains("success")){
                ResponseInfo responseInfo = JSONObject.parseObject(body, ResponseInfo.class);
                list.add(responseInfo);
            }else{
                MessageModel messageModel = JSONObject.parseObject(body, MessageModel.class);
                list.add(messageModel);
            }
    }
}
