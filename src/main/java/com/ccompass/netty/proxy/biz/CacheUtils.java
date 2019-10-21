package com.ccompass.netty.proxy.biz;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author albert on 10/16/19
 */
public class CacheUtils {

    public static final Map<ServerTypeEnum, List<ServerInfo>> SERVER_TYPE_ENUM_SERVER_INFO_MAP = new ConcurrentHashMap();

    static {
        SERVER_TYPE_ENUM_SERVER_INFO_MAP.put(ServerTypeEnum.MAIN, ImmutableList.<ServerInfo>builder()
                .add(ServerInfo.builder().host("127.0.0.1").port(7800).build())
                .build());

        SERVER_TYPE_ENUM_SERVER_INFO_MAP.put(ServerTypeEnum.ONE, ImmutableList.<ServerInfo>builder()
                .add(ServerInfo.builder().host("127.0.0.1").port(7810).build())
                .build());

        SERVER_TYPE_ENUM_SERVER_INFO_MAP.put(ServerTypeEnum.TWO, ImmutableList.<ServerInfo>builder()
                .add(ServerInfo.builder().host("127.0.0.1").port(7820).build())
                .build());

        SERVER_TYPE_ENUM_SERVER_INFO_MAP.put(ServerTypeEnum.THREE, ImmutableList.<ServerInfo>builder()
                .add(ServerInfo.builder().host("127.0.0.1").port(7830).build())
                .build());

        SERVER_TYPE_ENUM_SERVER_INFO_MAP.put(ServerTypeEnum.FOUR, ImmutableList.<ServerInfo>builder()
                .add(ServerInfo.builder().host("127.0.0.1").port(7100).build())
                .build());


    }



}
