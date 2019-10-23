package com.ccompass.netty.proxy;

import com.ccompass.netty.bizz.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.ccompass.netty.proxy.ExceptionCaughtHandler.closeOnFlush;
import static com.ccompass.netty.bizz.ChannelHelper.getAllRelationChannel;
import static com.ccompass.netty.bizz.ChannelHelper.getInboundChannelByArbitrarily;
import static com.ccompass.netty.bizz.ServiceTypeEnum.MAIN;

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

        ChannelInboundRealServerCache.put(inboundChannel);


        //循环创建从链路
        List<ServerInfo> serverInfos = CacheUtils.SERVER_TYPE_ENUM_SERVER_INFO_MAP.get(ServiceTypeEnum.ONE);

        Channel oneChannel = createSinkChannel(ctx, inboundChannel, serverInfos.get(0));

        ChannelInboundRealServerCache.put(inboundChannel, ServiceTypeEnum.ONE, oneChannel);

        List<ServerInfo> twoServerInfos = CacheUtils.SERVER_TYPE_ENUM_SERVER_INFO_MAP.get(ServiceTypeEnum.TWO);

        Channel twoChannel = createSinkChannel(ctx, inboundChannel, twoServerInfos.get(0));

        ChannelInboundRealServerCache.put(inboundChannel, ServiceTypeEnum.TWO, twoChannel);


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


        ChannelFutureListener channelFutureListener = future -> {
            if (future.isSuccess()) {
                // connection complete start to read first data
                log.info("future.isSuccess() " + future.isSuccess());
                ChannelInboundRealServerCache.put(inboundChannel, MAIN, outboundChannel);
                ChannelRealServerInboundCache.put(outboundChannel, inboundChannel);
                inboundChannel.read();
            } else {
                // Close the connection if the connection attempt has
                // failed.
                log.info("future.isSuccess() " + future.isSuccess());
                ChannelRealServerInboundCache.clear(inboundChannel);
                ChannelInboundRealServerCache.remove(inboundChannel);
                inboundChannel.close();
            }
        };

        f.addListener(channelFutureListener);
    }

    private Channel createSinkChannel(ChannelHandlerContext ctx, final Channel inboundChannel, ServerInfo serverInfo) {
        // Start the connection attempt.

        log.info("createSinkChannel");
        Channel sinkChannel = createOneChannel(ctx, inboundChannel, serverInfo);

        return sinkChannel;

    }

    // （从链路固定）创建一个链接
    private Channel createOneChannel(ChannelHandlerContext ctx, final Channel inboundChannel, ServerInfo serverInfo) {

        log.info("createOneChannel");
        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop())
                .channel(ctx.channel().getClass())
                .handler(new ProxyBackendHandler(inboundChannel))
                .option(ChannelOption.AUTO_READ, false);
        ChannelFuture f = b.connect(serverInfo.getHost(), serverInfo.getPort());
        Channel sinkChannel = f.channel();
        ChannelFutureListener listener = new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    // connection complete start to read first data
                    log.info("future.isSuccess() " + future.isSuccess());
                    inboundChannel.read();

                    ChannelInboundRealServerCache.put(inboundChannel, serverInfo.getServiceTypeEnum(), sinkChannel);
                    ChannelRealServerInboundCache.put(sinkChannel, inboundChannel);

                } else {
                    // Close the connection if the connection attempt has failed.
                    log.error("createOneChannel failed  " + future.isSuccess());
                    ChannelRealServerInboundCache.clear(inboundChannel);
                    ChannelInboundRealServerCache.remove(inboundChannel);

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
        //断开所有连接
        Set<Channel> allRelationChannel = getAllRelationChannel(ctx.channel());
        if (allRelationChannel.size() > 0) {
            for (Channel channel : allRelationChannel) {
                if (channel != null) {
                    closeOnFlush(channel);
                }
            }
        }
    }

    // 客户端发送数据
    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws InterruptedException {

        log.info("channelRead");

        ByteBuf buf = (ByteBuf) msg;
        List<ByteBuf> bufList = new ArrayList<ByteBuf>();
        for (int i = 0; i < getAllRelationChannel(ctx.channel()).size(); i++) {
            bufList.add(buf.copy());
        }

        ByteBuf parseParam = buf.copy();
        byte[] byteArray = new byte[parseParam.capacity()];
        parseParam.readBytes(byteArray);
        String result = new String(byteArray);

        String[] request = result.split(",");

        String playerId = request[0];

        Channel inboundChannel = getInboundChannelByArbitrarily(ctx.channel());


        String serviceType = request[1];

        int type = Integer.valueOf(serviceType);
        log.info("type {}", type);

        log.info(String.valueOf(getAllRelationChannel(inboundChannel)));

        Channel ch = ChannelInboundRealServerCache.get(inboundChannel, ServiceTypeEnum.getById(type));
        // 从连接发送数据
        if (msg != null) {

            if (ch.isActive()) {
                log.info("ch.isActive() " + ch.isActive());
                ByteBuf msg1 = bufList.get(1);
                ch
                        .writeAndFlush(msg1)
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

                log.info("ch.isActive() " + ch.isActive());
            }
        }
    }


}