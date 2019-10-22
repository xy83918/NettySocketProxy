package com.ccompass.netty.proxy.biz;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.ccompass.netty.proxy.ExceptionCaughtHandler.closeOnFlush;

/**
 * @author albert on 10/17/19
 */
public class ChannelRealServerInboundCache {

    public static final Map<Channel, Channel> PLAYER_MAP_CONNECTION_CHANNEL_MAP = new ConcurrentHashMap();

    public static void put(Channel realServer, Channel inbound) {
        PLAYER_MAP_CONNECTION_CHANNEL_MAP.put(realServer, inbound);
    }

    public static void clear(Channel inbound) {
        Map<ServerTypeEnum, Channel> serverTypeEnumChannelMap = ChannelInboundRealServerCache.CHANNEL_MAP_MAP.get(inbound);

        if (!serverTypeEnumChannelMap.isEmpty()) {
            serverTypeEnumChannelMap.values().forEach(v -> PLAYER_MAP_CONNECTION_CHANNEL_MAP.remove(v));
        }
    }

    public static void remove(Channel realServer) {
        Channel channel = get(realServer);
        Map<ServerTypeEnum, Channel> serverTypeEnumChannelMap = ChannelInboundRealServerCache.CHANNEL_MAP_MAP.get(channel);

        if (!serverTypeEnumChannelMap.isEmpty()) {
            serverTypeEnumChannelMap.values().forEach(v -> {
                closeOnFlush(v);
                PLAYER_MAP_CONNECTION_CHANNEL_MAP.remove(v);
            });
        }
    }

    public static Channel get(Channel realServer) {
        return PLAYER_MAP_CONNECTION_CHANNEL_MAP.get(realServer);
    }

}
