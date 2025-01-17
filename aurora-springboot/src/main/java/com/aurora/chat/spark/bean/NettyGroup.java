package com.aurora.chat.spark.bean;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: ChengLiang
 * @CreateTime: 2023-10-17  15:15
 * @Description: TODO
 * @Version: 1.0
 */

public class NettyGroup {

    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 存放用户与Chanel的对应信息，用于给指定用户发送消息
     */
    private static ConcurrentHashMap<String, Channel> channelMap = new ConcurrentHashMap<>();

    private NettyGroup() {
    }

    /**
     * 获取channel组
     */
    public static ChannelGroup getChannelGroup() {
        return channelGroup;
    }

    /**
     * 获取连接channel map
     */
    public static ConcurrentHashMap<String, Channel> getUserChannelMap() {
        return channelMap;
    }
}
