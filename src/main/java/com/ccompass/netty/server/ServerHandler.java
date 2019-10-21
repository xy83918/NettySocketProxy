package com.ccompass.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.net.InetAddress;

public class ServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {

        System.out.println("received");

        byte[] content = new byte[msg.content().capacity()];
        msg.content().readBytes(content);

        String s = String.valueOf(content);

        System.out.println(s);
        // 收到消息直接打印输出
        System.out.println(ctx.channel().remoteAddress() + " Say : " + s);

        // 返回客户端消息 - 我已经接收到了你的消息
        ctx.writeAndFlush(msg);
    }

    /*
     *
     * 覆盖 channelActive 方法 在channel被启用的时候触发 (在建立连接的时候)
     *
     * channelActive 和 channelInActive 在后面的内容中讲述，这里先不做详细的描述
     * */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        System.out.println("RamoteAddress : " + ctx.channel().remoteAddress() + " active !");


        ctx.writeAndFlush("Welcome to " + InetAddress.getLocalHost().getHostName() + " service!\n");

        super.channelActive(ctx);
    }
}