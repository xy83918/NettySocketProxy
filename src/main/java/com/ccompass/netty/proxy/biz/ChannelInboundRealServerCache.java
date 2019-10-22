package com.ccompass.netty.proxy.biz;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author albert on 10/17/19
 */
public class ChannelInboundRealServerCache {

    public static final Map<Channel, Map<ServerTypeEnum, Channel>> CHANNEL_MAP_MAP = new ConcurrentHashMap();
    public static final int MAP_CAPACITY = 3;

    public static void put(Channel inbound) {

        CHANNEL_MAP_MAP.put(inbound, new HashMap<>(MAP_CAPACITY));

    }

    public static void put(Channel inbound, ServerTypeEnum serverTypeEnum, Channel outbound) {

        Map<ServerTypeEnum, Channel> serverTypeEnumChannelMap = CHANNEL_MAP_MAP.get(inbound);
        if (serverTypeEnumChannelMap != null) {
            serverTypeEnumChannelMap.put(serverTypeEnum, outbound);
        } else {
            serverTypeEnumChannelMap = new HashMap<>(MAP_CAPACITY);
            serverTypeEnumChannelMap.put(serverTypeEnum, outbound);
        }
        CHANNEL_MAP_MAP.put(inbound, serverTypeEnumChannelMap);
    }

    public static Channel get(Channel channel, ServerTypeEnum serverTypeEnum) {
        return CHANNEL_MAP_MAP.get(channel).get(serverTypeEnum);
    }

    public static Set<Channel> getAll(Channel channel) {
        return CHANNEL_MAP_MAP.get(channel).values().stream().collect(Collectors.toSet());
    }

    public static void remove(Channel channel) {
        CHANNEL_MAP_MAP.remove(channel);
    }
}
