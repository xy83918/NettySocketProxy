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

 import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

 /**
  * @author albert on 10/22/19 10:34 AM
  */
 @Slf4j
 public final class Proxy {


     @Parameter(names = {"--host", "-h"})
     private static String host = "127.0.0.1";

     @Parameter(names = {"--port", "-p"})
     private static int port = 7800;

     @Parameter(names = {"--listen", "-l"})
     private static int listenPort = 7100;

     public static void main(String[] args) throws Exception {


         log.info("Proxy服务器开始启动：");


         Proxy main = new Proxy();

         JCommander.newBuilder()
                 .addObject(main)
                 .build()
                 .parse(args);

         log.info(String.valueOf(host));
         log.info(String.valueOf(port));
         log.info(String.valueOf(listenPort));


         EventLoopGroup bossGroup = new NioEventLoopGroup();
         EventLoopGroup workerGroup = new NioEventLoopGroup();
         //初始化从链路grops

         try {
             ServerBootstrap b = new ServerBootstrap();
             b.group(bossGroup, workerGroup)
                     .channel(NioServerSocketChannel.class)
                     //    .handler(new LoggingHandler(LogLevel.INFO))
                     .childHandler(new ProxyFrontEndInitializer())
                     .childOption(ChannelOption.AUTO_READ, false);

             ChannelFuture future = b.bind(listenPort).sync();

             log.info("Proxy服务器启动成功mian：监听端口：" + host);
             log.info("Proxy服务器启动成功port：监听端口：" + port);
             log.info("Proxy服务器启动成功：监听端口：" + listenPort);

             future.channel().closeFuture().sync();


         } finally {
             bossGroup.shutdownGracefully();
             workerGroup.shutdownGracefully();
         }


     }


 }