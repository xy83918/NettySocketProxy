package com.ccompass.netty.proxy;


import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import static com.ccompass.netty.proxy.ExceptionCaughtHandler.closeOnFlush;

/**
 * @author albert on 10/22/19 11:57 AM
 */
@Slf4j
public class ProxyBackendHandler extends ChannelInboundHandlerAdapter {

    private final Channel inboundChannel;

    public ProxyBackendHandler(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        log.info("received channelActive start ---");

        ctx.read();
        ctx.write(Unpooled.EMPTY_BUFFER);

        log.info("received channelActive finish ---");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("received channelInactive start ---");

        closeOnFlush(inboundChannel);

        log.info("received channelInactive finish ---");
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {

        log.info("channelRead inboundChannel {} {}", msg.getClass().getSimpleName());

        WebSocketFrame frame = new BinaryWebSocketFrame(Unpooled.EMPTY_BUFFER);
        if (msg instanceof BinaryWebSocketFrame) {
            log.info("BinaryWebSocketFrame");
            frame = (WebSocketFrame) msg;
        }

        log.info(String.valueOf(ctx));

//        Channel inboundChannel = getInboundChannelByArbitrarily(ctx.channel());
        if (inboundChannel.isActive()) {
            inboundChannel.writeAndFlush(frame.retain()).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("future.isSuccess() " + future.isSuccess());
                    inboundChannel.read();
                } else {
                    log.info("future.isSuccess() " + future.isSuccess());
                    inboundChannel.close();
                }
            });
        }
    }

}