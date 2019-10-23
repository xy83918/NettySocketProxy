package com.ccompass.netty.proxy;

import com.ccompass.netty.bizz.*;
import com.ccompass.netty.client.WebSocketClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ccompass.netty.bizz.ChannelHelper.getAllRelationChannel;
import static com.ccompass.netty.bizz.ChannelHelper.getInboundChannelByArbitrarily;
import static com.ccompass.netty.bizz.ServiceTypeEnum.MAIN;
import static com.ccompass.netty.proxy.ExceptionCaughtHandler.closeOnFlush;

/**
 * @author albert on 10/23/19 3:10 PM
 */
@Slf4j
public class ProxyFrontendHandler extends ChannelInboundHandlerAdapter {

    private final ServerInfo serverInfo;
    private volatile Channel outboundChannel;


    private AtomicInteger serviceTypeId = new AtomicInteger(2);

    public ProxyFrontendHandler(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
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

    private Channel createSinkChannel(ChannelHandlerContext ctx, final Channel inboundChannel, ServerInfo serverInfo) {
        // Start the connection attempt.

        log.info("createSinkChannel");
        Channel sinkChannel = createOneChannel(ctx, inboundChannel, serverInfo);

        return sinkChannel;

    }

    // （从链路固定）创建一个链接
    private Channel createOneChannel(ChannelHandlerContext ctx, final Channel inboundChannel, ServerInfo serverInfo) {

        log.info("createOneChannel");


        URI uri = getUri(serverInfo);

        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders());
        log.info(String.valueOf(handshaker));
        final WebSocketClientHandler handler = new WebSocketClientHandler(handshaker);

        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop())
                .channel(ctx.channel().getClass())
                .option(ChannelOption.AUTO_READ, false)
                .handler(new ProxyBackEndInitializer(inboundChannel, handler));

        ChannelFuture f = b.connect(serverInfo.getHost(), serverInfo.getPort());
        Channel sinkChannel = f.channel();
        ChannelFutureListener listener = future -> {
            if (future.isSuccess()) {
                // connection complete start to read first data
                log.info("future.isSuccess() " + future.isSuccess());
//                inboundChannel.read();

                ChannelInboundRealServerCache.put(inboundChannel, serverInfo.getServiceTypeEnum(), sinkChannel);
                ChannelRealServerInboundCache.put(sinkChannel, inboundChannel);

            } else {
                // Close the connection if the connection attempt has failed.
                log.error("createOneChannel failed  " + future.isSuccess());
                ChannelRealServerInboundCache.clear(inboundChannel);
                ChannelInboundRealServerCache.remove(inboundChannel);

                inboundChannel.close();


            }
        };
        f.addListener(listener);

        handler.handshakeFuture().addListener(future -> {
            if (future.isSuccess()) {
                // connection complete start to read first data
                log.info("handshakeFuture.isSuccess() " + future.isSuccess());
                sinkChannel.read();
            } else {
                // Close the connection if the connection attempt has failed.
                log.error("handshakeFuture failed  " + future.isSuccess());
                sinkChannel.close();
            }
        });


        return sinkChannel;

    }

    private URI getUri(ServerInfo serverInfo) {
        String URL = "ws://" + serverInfo.getHost() + ":" + serverInfo.getPort() + "/" + (serverInfo.getPath() == null ? "" : serverInfo.getPath());

        URI uri = null;
        try {
            uri = new URI(URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return uri;
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


        int type;

        if (serviceTypeId.get() == 1) {
            type = serviceTypeId.getAndIncrement();
        } else if (serviceTypeId.get() == 2) {
            type = serviceTypeId.getAndDecrement();
        } else {
            serviceTypeId.set(1);
            type = serviceTypeId.get();
        }

        log.info("type {}", type);


        Channel inboundChannel = getInboundChannelByArbitrarily(ctx.channel());
        log.info(String.valueOf(getAllRelationChannel(inboundChannel)));

        Channel ch = ChannelInboundRealServerCache.get(inboundChannel, ServiceTypeEnum.getById(type));

        // 从连接发送数据
        if (msg != null) {

            if (ch.isActive()) {
                log.info("ch.isActive() " + ch.isActive());
                ch
                        .writeAndFlush(msg)
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

    private void createMainChannel(ChannelHandlerContext ctx,
                                   final Channel inboundChannel) {
        // Start the connection attempt.

        log.info("createMainChannel");
        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop()).channel(ctx.channel().getClass())
                .handler(new ProxyBackendHandler(inboundChannel))
                .option(ChannelOption.AUTO_READ, false);
        ChannelFuture f = b.connect(serverInfo.getHost(), serverInfo.getPort());
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


}