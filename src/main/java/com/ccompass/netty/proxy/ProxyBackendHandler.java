package com.ccompass.netty.proxy;


import com.ccompass.netty.bizz.ChannelHelper;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
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

        log.info("channelActive");
        ctx.read();
        ctx.write(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("channelInactive");
        closeOnFlush(inboundChannel);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {

        log.info("channelRead");
        Channel inbound = ChannelHelper.getInboundChannelByArbitrarily(ctx.channel());
        inboundChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {

                log.info("future.isSuccess() " + future.isSuccess());
                ctx.channel().read();
            } else {

                log.info("future.isSuccess() " + future.isSuccess());
                future.channel().close();
            }
        });
    }

}