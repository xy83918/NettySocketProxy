package com.ccompass.netty.proxy;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author albert on 10/17/19
 */
public class ChannelPlayerCache {

    public static final Map<Channel, Integer> CHANNEL_PLAYER_MAP = new ConcurrentHashMap();

    public static void put(Channel channel, Integer playerId) {
        CHANNEL_PLAYER_MAP.put(channel, playerId);
    }

    public static Integer get(Channel channel) {
        return CHANNEL_PLAYER_MAP.get(channel);
    }

    public static void remove(Channel channel) {
        CHANNEL_PLAYER_MAP.remove(channel);
    }
}
