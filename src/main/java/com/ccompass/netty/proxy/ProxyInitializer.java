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

import com.ccompass.netty.proxy.biz.ServerInfo;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

public class ProxyInitializer extends ChannelInitializer<SocketChannel> {

    private final String remoteHost;
    private final int remotePort;

    public ProxyInitializer(ServerInfo serverInfo) {
        this.remoteHost = serverInfo.getHost();
        this.remotePort = serverInfo.getPort();
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(new IdleStateHandler(3600, 0, 0));

        //请求日志
        ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
        ch.pipeline().addLast(new ProxyFrontendHandler(remoteHost, remotePort));
        ch.pipeline().addLast(new ExceptionCaughtHandler());
    }
}