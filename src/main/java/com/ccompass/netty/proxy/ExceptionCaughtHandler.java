package com.ccompass.netty.proxy;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

import static com.ccompass.netty.bizz.ChannelHelper.getAllRelationChannel;

/**
 * @author albert on 10/22/19 2:36 PM
 */
@Slf4j
public class ExceptionCaughtHandler extends ChannelInboundHandlerAdapter {


    // 出异常的连接
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        log.error("exceptionCaught " + cause.getMessage(), cause);

        Set<Channel> allRelationChannel = getAllRelationChannel(ctx.channel());
        if (allRelationChannel.size() > 0) {
            for (Channel channel : allRelationChannel) {
                if (channel != null) {
                    closeOnFlush(channel);
                }
            }
        }
    }

    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    public static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(
                    ChannelFutureListener.CLOSE);
        }
    }

}