package com.ccompass.netty.bizz;

import io.netty.channel.Channel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.ccompass.netty.bizz.ChannelInboundRealServerCache.CHANNEL_MAP_MAP;


/**
 * @author albert on 10/17/19
 */
@NoArgsConstructor
@Slf4j
public class ChannelHelper {


    public static Channel getInboundChannel(Channel inbound) {
        Channel ThreeServerChannel = getServerChannel(inbound, ServiceTypeEnum.ZERO);
        log.info("ChannelHelper getInboundChannel {}", ThreeServerChannel);
        log.info("ChannelHelper getInboundChannel {} status {}", ThreeServerChannel.isActive(), ThreeServerChannel.isOpen());
        return ThreeServerChannel;
    }

    public static Channel getServerChannel(Channel inbound, ServiceTypeEnum serviceTypeEnum) {

        Map<ServiceTypeEnum, Channel> integerChannelMap = CHANNEL_MAP_MAP.get(inbound);

        if (integerChannelMap == null) {
            integerChannelMap = new ConcurrentHashMap<>();
        }

        return integerChannelMap.get(serviceTypeEnum.getId());
    }

    public static Channel setInboundChannel(Channel inbound, Channel channel) {
        return setServerChannel(inbound, ServiceTypeEnum.ZERO, channel);
    }

    public static Channel setServerChannel(Channel inbound, ServiceTypeEnum serviceTypeEnum, Channel channel) {

        Map<ServiceTypeEnum, Channel> integerChannelMap = CHANNEL_MAP_MAP.get(inbound);

        if (integerChannelMap == null) {
            integerChannelMap = new ConcurrentHashMap<>();
        }
        integerChannelMap.put(serviceTypeEnum, channel);
        CHANNEL_MAP_MAP.put(inbound, integerChannelMap);
        return channel;
    }

    public static Channel getOneChannel(Channel inbound) {
        return getServerChannel(inbound, ServiceTypeEnum.ONE);
    }

    public static Channel setOneChannel(Channel inbound, Channel channel) {

        return setServerChannel(inbound, ServiceTypeEnum.ONE, channel);
    }

    public static Channel getTwoChannel(Channel inbound) {
        return getServerChannel(inbound, ServiceTypeEnum.TWO);
    }

    public static Channel setTwoChannel(Channel inbound, Channel channel) {
        return setServerChannel(inbound, ServiceTypeEnum.TWO, channel);
    }

    public static Channel getThreeChannel(Channel inbound) {
        return getServerChannel(inbound, ServiceTypeEnum.THREE);
    }

    public static Channel setThreeChannel(Channel inbound, Channel channel) {
        return setServerChannel(inbound, ServiceTypeEnum.THREE, channel);
    }

    public static Set<Channel> getAllRelationChannel(Channel arbitrarily) {

        Set set = new HashSet();
        set.add(arbitrarily);

        Channel channel = ChannelRealServerInboundCache.get(arbitrarily);
        if (channel != null) {
            set.addAll(ChannelInboundRealServerCache.getAll(channel));
        } else {
            set.addAll(ChannelInboundRealServerCache.getAll(arbitrarily));
        }

        return set;

    }


    public static Channel getInboundChannelByArbitrarily(Channel arbitrarily) {

        Channel channel = ChannelRealServerInboundCache.get(arbitrarily);
        if (channel != null) {
            return channel;
        } else {
            return arbitrarily;
        }

    }


}
