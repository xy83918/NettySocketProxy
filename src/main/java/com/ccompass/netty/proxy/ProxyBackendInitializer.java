/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.ccompass.netty.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author albert on 10/23/19 3:10 PM
 */
public class ProxyBackendInitializer extends ChannelInitializer<SocketChannel> {


    private final ProxyBackendWebSocketHandler handler;
    private final Channel inboundChannel;

    public ProxyBackendInitializer(Channel inboundChannel, ProxyBackendWebSocketHandler handler) {
        this.inboundChannel = inboundChannel;
        this.handler = handler;

    }

    @Override
    public void initChannel(SocketChannel ch) {

        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new LoggingHandler(LogLevel.TRACE));
        pipeline.addLast(new HttpClientCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(handler);
        pipeline.addLast(new ProxyBackendHandler(inboundChannel));
        pipeline.addLast(new ExceptionCaughtHandler());


    }
}