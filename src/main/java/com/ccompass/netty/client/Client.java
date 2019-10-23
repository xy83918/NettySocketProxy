package com.ccompass.netty.client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.websocketx.*;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

@Slf4j
public class Client {

    public static final String FINISH_FLAG = "\r\n";
    public static String host = "127.0.0.1";
    public static int port = 7100;


    @Parameter(names = {"--playerId", "-p"})
    private static String playerId;

    /**
     * @param args
     * @throws InterruptedException
     * @throws IOException
     */
    public static void main(String[] args) throws InterruptedException, IOException {


        Client main = new Client();

        JCommander.newBuilder()
                .addObject(main)
                .build()
                .parse(args);

        log.info(String.valueOf(playerId));

        String URL = "ws://" + host + ":" + port + "/";

        URI uri = null;
        try {
            uri = new URI(URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders());
        final WebSocketClientHandler handler = new WebSocketClientHandler(handshaker);


        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientInitializer(handler));

            // 连接服务端
            Channel ch = b.connect(host, port).sync().channel();

            log.info("start done");

            //数据传输格式  playerId,serviceId,data

            //console输入 1,data 2,data


            // 控制台输入
            handler.handshakeFuture().sync();

            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            for (; ; ) {
                String msg = console.readLine();
                if (msg == null) {
                    break;
                } else if ("bye".equals(msg.toLowerCase())) {
                    ch.writeAndFlush(new CloseWebSocketFrame());
                    ch.closeFuture().sync();
                    break;
                } else if ("ping".equals(msg.toLowerCase())) {
                    WebSocketFrame frame = new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[]{8, 1, 8, 1}));
                    ch.writeAndFlush(frame);
                } else {
                    log.info("msg : " + msg);
                    WebSocketFrame frame = new BinaryWebSocketFrame(Unpooled.copiedBuffer(msg.getBytes(Charset.forName("utf-8"))));
                    ch.writeAndFlush(frame);
                }
            }
        } finally {
            // The connection is closed automatically on shutdown.
            group.shutdownGracefully();
        }
    }
}