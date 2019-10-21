package com.ccompass.netty.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static com.ccompass.netty.proxy.ChannelCacheManager.CONNECTION_CHANNEL_MAP;

@Slf4j
public class ProxyFrontendHandler extends ChannelInboundHandlerAdapter {

    private final String remoteHost;
    private final int remotePort;
    private volatile Channel outboundChannel;

    public ProxyFrontendHandler(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    // 连接服务器
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("channelActive");

        final Channel inboundChannel = ctx.channel();
        // Start the connection attempt.

        CONNECTION_CHANNEL_MAP.put(ServerTypeEnum.ZERO.getId(), inboundChannel);

        createMainChannel(ctx, inboundChannel);

        //循环创建从链路
        List<ServerInfo> serverInfos = CacheUtils.SERVER_TYPE_ENUM_SERVER_INFO_MAP.get(ServerTypeEnum.ONE);

        Channel channel = createSinkChannel(ctx, inboundChannel, serverInfos.get(0));

        CONNECTION_CHANNEL_MAP.put(ServerTypeEnum.ONE.getId(), channel);


    }

    private void createMainChannel(ChannelHandlerContext ctx,
                                   final Channel inboundChannel) {
        // Start the connection attempt.

        log.info("createMainChannel");
        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop()).channel(ctx.channel().getClass())
                .handler(new ProxyBackendHandler(inboundChannel))
                .option(ChannelOption.AUTO_READ, false);
        ChannelFuture f = b.connect(remoteHost, remotePort);
        outboundChannel = f.channel();


        CONNECTION_CHANNEL_MAP.put(ServerTypeEnum.MAIN.getId(), outboundChannel);

        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    // connection complete start to read first data
                    log.info("future.isSuccess() " + future.isSuccess());
                    inboundChannel.read();
                } else {
                    // Close the connection if the connection attempt has
                    // failed.
                    log.info("future.isSuccess() " + future.isSuccess());
                    inboundChannel.close();
                }
            }
        });
    }

    private Channel createSinkChannel(ChannelHandlerContext ctx, final Channel inboundChannel, ServerInfo serverInfo) {
        // Start the connection attempt.

        log.info("createSinkChannel");
        Channel sinkChannel = createOneChannel(ctx, inboundChannel, serverInfo);

        return sinkChannel;

    }

    // （从链路固定）创建一个链接
    private Channel createOneChannel(ChannelHandlerContext ctx, final Channel inboundChannel, ServerInfo addressPort) {

        log.info("createOneChannel");
        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop())
                .channel(ctx.channel().getClass())
                .handler(new ProxyBackendHandler(inboundChannel))
                .option(ChannelOption.AUTO_READ, false);
        ChannelFuture f = b.connect(addressPort.getHost(), addressPort.getPort());
        Channel sinkChannel = f.channel();
        ChannelFutureListener listener = new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    // connection complete start to read first data
                    log.info("future.isSuccess() " + future.isSuccess());
                    inboundChannel.read();
                } else {
                    // Close the connection if the connection attempt has failed.
                    log.info("future.isSuccess() " + future.isSuccess());
                    log.info("createOneChannel failed  " + future.isSuccess());
                    inboundChannel.close();
                }
            }
        };
        f.addListener(listener);
        return sinkChannel;

    }

    // 中断的连接
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        log.info("channelInactive");
        closeOnFlush(ctx.channel());
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
        // 主从链路一对一
        if (CONNECTION_CHANNEL_MAP.size() > 0) {
            for (Channel channel : CONNECTION_CHANNEL_MAP.values()) {
                if (channel != null) {
                    closeOnFlush(channel);
                }
            }
        }
    }

    // 客户端发送数据
    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {

        log.info("channelRead");

        ByteBuf buf = (ByteBuf) msg;
        List<ByteBuf> bufList = new ArrayList<ByteBuf>();
        for (int i = 0; i < CONNECTION_CHANNEL_MAP.size(); i++) {
            bufList.add(buf.copy());
        }

        if (outboundChannel.isActive() && outboundChannel.isOpen()) {
            outboundChannel.writeAndFlush(msg).addListener(
                    new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) {
                            if (future.isSuccess()) {
                                log.info("future.isSuccess() " + future.isSuccess());
                                ctx.channel().read();
                            } else {
                                log.info("future.isSuccess() " + future.isSuccess());
                                future.channel().close();
                            }
                        }
                    });
        } else {
            final Channel inboundChannel = ctx.channel();
            // Start the connection attempt.
            Bootstrap b = new Bootstrap();
            b.group(inboundChannel.eventLoop()).channel(ctx.channel().getClass())
                    .handler(new ProxyBackendHandler(inboundChannel))
                    .option(ChannelOption.AUTO_READ, false);
            ChannelFuture f = b.connect(remoteHost, remotePort);
            outboundChannel = f.channel();
            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        // connection complete start to read first data
                        log.info("future.isSuccess() " + future.isSuccess());
                        inboundChannel.read();
                    } else {
                        // Close the connection if the connection attempt has
                        // failed.
                        log.info("future.isSuccess() " + future.isSuccess());
                        inboundChannel.close();
                    }
                }
            });
            outboundChannel.writeAndFlush(msg).addListener(
                    new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) {
                            if (future.isSuccess()) {
                                log.info("future.isSuccess() " + future.isSuccess());
                                ctx.channel().read();
                            } else {
                                log.info("future.isSuccess() " + future.isSuccess());
                                future.channel().close();
                            }
                        }
                    });

        }
        final Channel inboundChannel = ctx.channel();
        // 从连接发送数据
        if (msg != null) {

            Channel ch = CONNECTION_CHANNEL_MAP.get(ServerTypeEnum.ONE.getId());

            log.info(String.valueOf(CONNECTION_CHANNEL_MAP));
            if (ch.isActive()) {
                ch
                        .writeAndFlush(
                                bufList.get(0))
                        .addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {
                                if (future.isSuccess()) {
                                    log.info("future.isSuccess() " + future.isSuccess());
                                    // was able to flush out data, start to read the next chunk
                                    ctx.channel().read();
                                } else {
                                    log.info("future.isSuccess() " + future.isSuccess());
                                    future.channel().close();
                                }
                            }
                        });
            } else {

                List<ServerInfo> serverInfos = CacheUtils.SERVER_TYPE_ENUM_SERVER_INFO_MAP.get(ServerTypeEnum.FOUR);
                ch = createSinkChannel(ctx, inboundChannel, serverInfos.get(0));
                //讲各个从链路分别存放到各自的group中
                ch.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            // was able to flush out data, start to read the next chunk
                            log.info("future.isSuccess() " + future.isSuccess());
                            ctx.channel().read();
                        } else {
                            log.info("future.isSuccess() " + future.isSuccess());
                            future.channel().close();
                        }
                    }
                });
            }
        }
    }

    // 心跳检查
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        // TODO Auto-generated method stub

        Channel channel = ctx.channel();
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.READER_IDLE)) {
                channel.close();
                log.info("**********心跳检测读失败，强制关闭终端连接");
            } else if (event.state().equals(IdleState.WRITER_IDLE)) {
                channel.close();
                log.info("**********心跳检测写失败，强制关闭终端连接");
            } else if (event.state().equals(IdleState.ALL_IDLE)) {
                log.info("**********心跳检测，检测数据发送");
                // 发送心跳
                ctx.channel().write("ping\n");
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    // 出异常的连接
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        log.info("**********exceptionCaught，异常信息：" + cause.getMessage(), cause);
		/*closeOnFlush(ctx.channel());
		// 出异常关闭主从链接
		if (outboundChannel != null) {
			closeOnFlush(outboundChannel);
		}*/
        // 主从链路一对一
        if (CONNECTION_CHANNEL_MAP.size() > 0) {
            for (Channel channel : CONNECTION_CHANNEL_MAP.values()) {
                if (channel != null) {
                    closeOnFlush(channel);
                }
            }
        }
    }

    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(
                    ChannelFutureListener.CLOSE);
        }
    }

}