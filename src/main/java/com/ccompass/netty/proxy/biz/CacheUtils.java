package com.ccompass.netty.proxy.biz;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.ccompass.netty.proxy.biz.ServerTypeEnum.*;

/**
 * @author albert on 10/16/19
 */
public class CacheUtils {

    public static final Map<ServerTypeEnum, List<ServerInfo>> SERVER_TYPE_ENUM_SERVER_INFO_MAP = new ConcurrentHashMap();

    static {
        SERVER_TYPE_ENUM_SERVER_INFO_MAP.put(MAIN, ImmutableList.<ServerInfo>builder()
                .add(ServerInfo.builder().serverTypeEnum(MAIN).host("127.0.0.1").port(7800).build())
                .build());

        SERVER_TYPE_ENUM_SERVER_INFO_MAP.put(ONE, ImmutableList.<ServerInfo>builder()
                .add(ServerInfo.builder().serverTypeEnum(ONE).host("127.0.0.1").port(7810).build())
                .build());

        SERVER_TYPE_ENUM_SERVER_INFO_MAP.put(TWO, ImmutableList.<ServerInfo>builder()
                .add(ServerInfo.builder().serverTypeEnum(TWO).host("127.0.0.1").port(7820).build())
                .build());

        SERVER_TYPE_ENUM_SERVER_INFO_MAP.put(THREE, ImmutableList.<ServerInfo>builder()
                .add(ServerInfo.builder().serverTypeEnum(THREE).host("127.0.0.1").port(7830).build())
                .build());

        SERVER_TYPE_ENUM_SERVER_INFO_MAP.put(FOUR, ImmutableList.<ServerInfo>builder()
                .add(ServerInfo.builder().serverTypeEnum(FOUR).host("127.0.0.1").port(7100).build())
                .build());


    }


}
