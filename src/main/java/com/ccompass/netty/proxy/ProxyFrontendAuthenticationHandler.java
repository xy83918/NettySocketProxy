package com.ccompass.netty.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author albert on 10/23/19 3:10 PM
 */
@Slf4j
public class ProxyFrontendAuthenticationHandler extends ChannelInboundHandlerAdapter {


    private AtomicInteger serviceTypeId = new AtomicInteger(2);


    // 客户端发送数据
    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws InterruptedException {

        log.info("ProxyAuthenticationHandler channelRead");

        log.info(msg.getClass().getSimpleName());

        BinaryWebSocketFrame frame = (BinaryWebSocketFrame) msg;

        BinaryWebSocketFrame copy = frame.copy();

        ByteBuf bf = frame.content();
        byte[] byteArray = new byte[bf.capacity()];
        bf.readBytes(byteArray);
        String result = new String(byteArray);

        if (result.equals("token")) {
            ctx.pipeline().remove(this);
            log.info("ctx pipeline {}", ctx.pipeline());
            ctx.channel().writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer("auth success".getBytes()))).sync();
        } else {
            ctx.fireChannelRead(copy);
        }



    }

}