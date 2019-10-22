package com.ccompass.netty.client;

import com.ccompass.netty.proxy.ExceptionCaughtHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        //请求日志
        pipeline.addLast(new LoggingHandler(LogLevel.TRACE));
        /*
         * 这个地方的 必须和服务端对应上。否则无法正常解码和编码
         * */
        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        pipeline.addLast("decoder", new StringDecoder());
        pipeline.addLast("encoder", new StringEncoder());
        
        // 客户端的逻辑
        pipeline.addLast("handler", new ClientHandler());

        pipeline.addLast("exceptionCaughtHandler", new ExceptionCaughtHandler());
    }
}