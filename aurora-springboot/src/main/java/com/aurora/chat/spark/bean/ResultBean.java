package com.aurora.chat.spark.bean;

import com.alibaba.fastjson.JSON;
import com.aurora.chat.spark.SparkConfig;
import com.aurora.chat.spark.SparkWebClient;
import com.aurora.chat.spark.SparkWebSocketListener;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: ChengLiang
 * @CreateTime: 2023-05-22  11:04
 * @Description: TODO
 * @Version: 1.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ResultBean<T> {

    private String errorCode;

    private String message;

    private T data;

    public ResultBean(T data) {
        this.errorCode = ErrorMessage.SUCCESS.getErrorCode();
        this.message = ErrorMessage.SUCCESS.getMessage();
        this.data = data;
    }

    public ResultBean(ErrorMessage errorMessage, T data) {
        this.errorCode = errorMessage.getErrorCode();
        this.message = errorMessage.getMessage();
        this.data = data;
    }


    public static <T> ResultBean success(T data) {
        ResultBean resultBean = new ResultBean(data);
        return resultBean;
    }

    public static <T> ResultBean fail(T data) {
        ResultBean resultBean = new ResultBean(ErrorMessage.FAIL.getErrorCode(), ErrorMessage.FAIL.getMessage(), data);
        return resultBean;
    }

    public enum ErrorMessage {

        SUCCESS("0", "success"),
        FAIL("001", "fail"),
        NOAUTH("1001", "非法访问");

        private String errorCode;
        private String message;

        ErrorMessage(String errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
    
    /**
     * @Author: ChengLiang
     * @CreateTime: 2023-10-17  15:58
     * @Description: TODO
     * @Version: 1.0
     */
    @Slf4j
    @Service
    public static class PushServiceImpl implements PushService {
    
        @Autowired
        private SparkConfig sparkConfig;
    
        @Autowired
        private SparkWebClient sparkWebClient;
    
        @Override
        public void pushToOne(String uid, String text) {
            if (StringUtils.isEmpty(uid) || StringUtils.isEmpty(text)) {
                log.error("uid或text均不能为空");
                throw new RuntimeException("uid或text均不能为空");
            }
            ConcurrentHashMap<String, Channel> userChannelMap = NettyGroup.getUserChannelMap();
            for (String channelId : userChannelMap.keySet()) {
                if (channelId.equals(uid)) {
                    Channel channel = userChannelMap.get(channelId);
                    if (channel != null) {
                        ResultBean success = success(text);
                        channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(success)));
                        log.info("信息发送成功：{}", JSON.toJSONString(success));
                    } else {
                        log.error("该id对于channelId不存在！");
                    }
                    return;
                }
            }
            log.error("该用户不存在！");
        }
    
        @Override
        public void pushToAll(String text) {
            String trim = text.trim();
            ResultBean success = success(trim);
            NettyGroup.getChannelGroup().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(success)));
            log.info("信息推送成功：{}", JSON.toJSONString(success));
        }
    
        //测试账号只有2个并发，此处只使用一个，若是生产环境允许多个并发，可以采用分布式锁
        @Override
        public synchronized ResultBean pushMessageToXFServer(String uid, String text) {
            RoleContent userRoleContent = RoleContent.createUserRoleContent(text);
            ArrayList<RoleContent> questions = new ArrayList<>();
            questions.add(userRoleContent);
            SparkWebSocketListener sparkWebSocketListener = new SparkWebSocketListener();
            WebSocket webSocket = sparkWebClient.sendMsg(uid, questions, sparkWebSocketListener);
            if (webSocket == null) {
                log.error("webSocket连接异常");
                fail("请求异常，请联系管理员");
            }
            try {
                int count = 0;
                int maxCount = sparkConfig.getMaxResponseTime() * 5;
                while (count <= maxCount) {
                    Thread.sleep(200);
                    if (sparkWebSocketListener.isWsCloseFlag()) {
                        break;
                    }
                    count++;
                }
                if (count > maxCount) {
                    return fail("响应超时，请联系相关人员");
                }
                return success(sparkWebSocketListener.getAnswer());
            } catch (Exception e) {
                log.error("请求异常：{}", e);
            } finally {
                webSocket.close(1000, "");
            }
            return success("");
        }
    }
    
    public static interface PushService {
    
        void pushToOne(String uid, String text);
    
        void pushToAll(String text);
    
        ResultBean pushMessageToXFServer(String uid, String text);
    }
}
