package com.ccompass.netty.server;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Server {

    /**
     * 服务端监听的端口地址
     */

    @Parameter(names = {"--port", "-p"})
    private static int portNumber = 7878;

    public static void main(String[] args) throws InterruptedException {


        Server main = new Server();

        JCommander.newBuilder()
                .addObject(main)
                .build()
                .parse(args);

        log.info(String.valueOf(portNumber));

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_LINGER, 65535)
                    .childOption(ChannelOption.SO_RCVBUF, 1024 * 512)
                    .childOption(ChannelOption.SO_SNDBUF, 1024 * 512)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast("http-codec", new HttpServerCodec()); // Http消息编码解码
                            pipeline.addLast("aggregator", new HttpObjectAggregator(65536)); // Http消息组装
                            pipeline.addLast("http-chunked", new ChunkedWriteHandler()); // WebSocket通信支持
                            pipeline.addLast("handler", new ServerHandler()); // WebSocket通信支持
                        }
                    });
            // 服务器绑定端口监听
            ChannelFuture f = b.bind(portNumber).sync();
            System.out.println("服务器启动成功：监听端口：" + portNumber);
            // 监听服务器关闭监听
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}