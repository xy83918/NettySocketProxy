package com.ccompass.netty.bizz;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author albert on 10/17/19
 */
public class RealServerChannelCache {

    public static final Map<ServiceTypeEnum, TreeSet<Channel>> SERVICE_TYPE_ENUM_SET_MAP = new ConcurrentHashMap();
    public static final  TreeSet<Channel> CHANNEL_TREE_SET = new TreeSet<>();


    public static void add(ServiceTypeEnum serviceTypeEnum, Channel channel) {

        TreeSet<Channel> channels = SERVICE_TYPE_ENUM_SET_MAP.get(serviceTypeEnum);

        if (channels == null) {
            channels = new TreeSet<>();
            channels.add(channel);
        } else {
            channels.add(channel);
        }

    }
    public static Channel get(ServiceTypeEnum serviceTypeEnum) {

        TreeSet<Channel> channels = SERVICE_TYPE_ENUM_SET_MAP.get(serviceTypeEnum);

        return channels.first();

    }

}
