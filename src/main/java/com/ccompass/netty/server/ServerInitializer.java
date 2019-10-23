package com.ccompass.netty.server;

import com.ccompass.netty.proxy.ExceptionCaughtHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        //请求日志
        pipeline.addLast(new LoggingHandler(LogLevel.TRACE));
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
        pipeline.addLast(new WebSocketServerProtocolHandler("/", null, true));
        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
        // 自己的逻辑Handler
        pipeline.addLast("handler", new WebSocketFrameHandler());
        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
        pipeline.addLast("exceptionCaughtHandler", new ExceptionCaughtHandler());

    }
}