package com.ccompass.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledUnsafeDirectByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class NettyClient implements Cloneable {
    //有效连接
    public static int activeConnects = 0;
    //终端请求次数
    public static int connects = 0;
    //出错的连接束
    public static int exceptions = 0;
    public static ChannelGroup group = new DefaultChannelGroup("server-group", null);
    //无效连接
    public static int inactiveConnects = 0;
    public static ChannelGroup otherGroup = new DefaultChannelGroup("server-group", null);
    public static ChannelGroup otherGroup2 = new DefaultChannelGroup("server-group", null);
    public static int requests = 0;
    public static List<ChannelGroup> sinkGroups = new ArrayList<ChannelGroup>();
    private static Channel channel;
    private static List<Channel> channels;
    private static List<Channel> channels2;
    private static NettyClient instance = null;
    private static List<List<Channel>> sinkChannels;
    private String host;
    // 服务器端口
    private Integer port;
    //单例
    private NettyClient() {
    }

    public NettyClient(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public List<List<Channel>> getSinkChannels() {
        return sinkChannels;
    }

    public static void setSinkChannels(List<List<Channel>> sinkChannels) {
        NettyClient.sinkChannels = sinkChannels;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        NettyClient.channels = channels;
    }

    public List<Channel> getChannels2() {
        return channels2;
    }

    public void setChannels2(List<Channel> channels2) {
        NettyClient.channels2 = channels2;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * 连接从服务器
     */
    public void connect() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            if (this.channel == null || (!NettyClient.getInstance().getChannel().isOpen())) {
                Bootstrap b = new Bootstrap();
                b.group(group).channel(NioSocketChannel.class).handler(new ClientInitializer());
                // 连接服务端
                Channel ch = b.connect(host, port).sync().channel();
                this.channel = ch;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // The connection is closed automatically on shutdown.
            //   group.shutdownGracefully();
        }
    }

    public static synchronized NettyClient getInstance() {
        if (instance == null) {
            instance = new NettyClient();
        }
        return instance;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    /**
     * 发送消息去从的代理服务器
     *
     * @param msg
     * @throws Exception
     */
    public void sendMsg(Object msg) throws Exception {
        if (msg != null && msg instanceof UnpooledUnsafeDirectByteBuf) {
            UnpooledUnsafeDirectByteBuf buf = (UnpooledUnsafeDirectByteBuf) msg;
            this.channel.writeAndFlush(buf.copy());
        } else {
            ByteBuf oc = (ByteBuf) msg;
            this.channel.writeAndFlush(oc.copy());
        }
    }


}
