package com.ccompass.netty.client;

import lombok.extern.slf4j.Slf4j;

import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class CheckSinkChannel extends TimerTask {
    private String host = "";
    private Integer port;

    public CheckSinkChannel(String host, Integer port) {
        super();
        this.host = host;
        this.port = port;
    }

    public static void main(String[] args) {
        Timer timer = new Timer();
        long delay1 = 1 * 1000;
        long period1 = 3000;
        // 从现在开始 1 秒钟之后，每隔 1 秒钟执行一次 job1
        timer.schedule(new CheckSinkChannel("127.0.0.1", 8001), delay1, period1);
    }

    @Override
    public void run() {
        //System.err.println("dodo:"+NettyClient.getInstance().getChannel()==null||(!NettyClient.getInstance().getChannel().isOpen()));
		/*if(NettyClient.getInstance().getChannel()==null||(!NettyClient.getInstance().getChannel().isOpen())){
			NettyClient client=NettyClient.getInstance();
	    	client.setHost(host);
	    	client.setPort(port);
			client.connect();
		}*/
        log.info("*******************************************************");
        log.info("************尝试连接过数量：" + NettyClient.connects + "  *************");
        log.info("************总请求次数数量：" + NettyClient.requests + "  ******************");
        log.info("************在线的连接数量：" + (NettyClient.activeConnects - NettyClient.inactiveConnects) + "  ******************");
        log.info("************断开的连接数量：" + NettyClient.inactiveConnects + "  ******************");
        log.info("************出错的连接数量：" + NettyClient.exceptions + "  ******************");
        log.info("************主链路连接数量：" + NettyClient.group.size() + "  ******************");
        for (int i = 0; i < NettyClient.sinkGroups.size(); i++) {
            int j = i + 1;
            log.info("************从链路" + j + "连接数量：" + NettyClient.sinkGroups.get(i).size() + "  ******************");
        }
        log.info("*******************************************************");
    }
}
