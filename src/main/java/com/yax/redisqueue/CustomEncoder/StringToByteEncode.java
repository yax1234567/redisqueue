package com.yax.redisqueue.CustomEncoder;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


public class StringToByteEncode extends MessageToByteEncoder<String> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, String json, ByteBuf byteBuf) throws Exception {
        //防止tcp粘包，拆包
        String jsonData= json+"\n\r";
        byte[] b=jsonData.getBytes();
        byteBuf.writeBytes(b);
    }
}
