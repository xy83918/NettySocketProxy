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
 import com.ccompass.netty.proxy.biz.ServerTypeEnum;
 import io.netty.bootstrap.ServerBootstrap;
 import io.netty.channel.ChannelFuture;
 import io.netty.channel.ChannelOption;
 import io.netty.channel.EventLoopGroup;
 import io.netty.channel.nio.NioEventLoopGroup;
 import io.netty.channel.socket.nio.NioServerSocketChannel;
 import lombok.extern.slf4j.Slf4j;

 import java.util.List;

 import static com.ccompass.netty.proxy.biz.CacheUtils.SERVER_TYPE_ENUM_SERVER_INFO_MAP;

 @Slf4j
 public final class Proxy {
     public static void main(String[] args) throws Exception {

         log.info("Proxy服务器开始启动：");

         EventLoopGroup bossGroup = new NioEventLoopGroup();
         EventLoopGroup workerGroup = new NioEventLoopGroup();
         //初始化从链路grops

         List<ServerInfo> serverInfos = SERVER_TYPE_ENUM_SERVER_INFO_MAP.get(ServerTypeEnum.MAIN);

         Integer port = SERVER_TYPE_ENUM_SERVER_INFO_MAP.get(ServerTypeEnum.FOUR).get(0).getPort();
         try {
             ServerBootstrap b = new ServerBootstrap();
             b.group(bossGroup, workerGroup)
                     .channel(NioServerSocketChannel.class)
                     //    .handler(new LoggingHandler(LogLevel.INFO))
                     .childHandler(new ProxyInitializer(serverInfos.get(0)))
                     .childOption(ChannelOption.AUTO_READ, false);

             ChannelFuture future = b.bind(port).sync();
             log.info("Proxy服务器启动成功：监听端口：" + port);

             future.channel().closeFuture().sync();


         } finally {
             bossGroup.shutdownGracefully();
             workerGroup.shutdownGracefully();
         }


     }


 }