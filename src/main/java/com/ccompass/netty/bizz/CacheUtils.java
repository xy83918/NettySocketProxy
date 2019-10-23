package com.ccompass.netty.bizz;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.ccompass.netty.bizz.ServiceTypeEnum.*;


/**
 * @author albert on 10/16/19
 */
public class CacheUtils {

    public static final Map<ServiceTypeEnum, List<ServerInfo>> SERVER_TYPE_ENUM_SERVER_INFO_MAP = new ConcurrentHashMap();

    static {
        SERVER_TYPE_ENUM_SERVER_INFO_MAP.put(MAIN, ImmutableList.<ServerInfo>builder()
                .add(ServerInfo.builder().serviceTypeEnum(MAIN).host("127.0.0.1").port(7800).build())
                .build());

        SERVER_TYPE_ENUM_SERVER_INFO_MAP.put(ONE, ImmutableList.<ServerInfo>builder()
                .add(ServerInfo.builder().serviceTypeEnum(ONE).host("127.0.0.1").port(7810).build())
                .build());

        SERVER_TYPE_ENUM_SERVER_INFO_MAP.put(TWO, ImmutableList.<ServerInfo>builder()
                .add(ServerInfo.builder().serviceTypeEnum(TWO).host("127.0.0.1").port(7820).build())
                .build());

        SERVER_TYPE_ENUM_SERVER_INFO_MAP.put(THREE, ImmutableList.<ServerInfo>builder()
                .add(ServerInfo.builder().serviceTypeEnum(THREE).host("127.0.0.1").port(7830).build())
                .build());

        SERVER_TYPE_ENUM_SERVER_INFO_MAP.put(FOUR, ImmutableList.<ServerInfo>builder()
                .add(ServerInfo.builder().serviceTypeEnum(FOUR).host("127.0.0.1").port(7100).build())
                .build());


    }


}
