package com.ccompass.netty.client;

import com.ccompass.netty.proxy.ExceptionCaughtHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class ClientInitializer extends ChannelInitializer<SocketChannel> {

    private WebSocketClientHandler handler;

    public ClientInitializer(WebSocketClientHandler handler) {
        this.handler = handler;
    }


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        //请求日志
        pipeline.addLast(new LoggingHandler(LogLevel.TRACE));
        /*
         * 这个地方的 必须和服务端对应上。否则无法正常解码和编码
         * */
        pipeline.addLast(
                new HttpClientCodec(),
                new HttpObjectAggregator(65535));

        // 客户端的逻辑
        pipeline.addLast("handler", handler);

        pipeline.addLast("exceptionCaughtHandler", new ExceptionCaughtHandler());
    }
}