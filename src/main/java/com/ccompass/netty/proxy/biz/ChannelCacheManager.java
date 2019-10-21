package com.ccompass.netty.proxy.biz;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author albert on 10/21/19
 */
public class ChannelCacheManager {


    public static final Map<Integer, Channel> CONNECTION_CHANNEL_MAP = new ConcurrentHashMap();


}
