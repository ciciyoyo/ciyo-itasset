package com.ciyocloud.message.websocket;

import cn.hutool.core.map.MapUtil;
import com.ciyocloud.common.constant.RedisKeyConstants;
import com.ciyocloud.common.util.JsonUtils;
import com.ciyocloud.common.util.RedisUtils;
import com.ciyocloud.common.util.SpringContextUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * webscoket session 管理
 *
 * @author codeck
 */
@Slf4j
@UtilityClass
public class WsSessionManager {
    private static final String REDIS_TOPIC_NAME = "wsSocketHandler";
    /**
     * 保存连接 session 的地方
     */
    private static final ConcurrentHashMap<String, WebSocketSession> SESSION_POOL = new ConcurrentHashMap<>();

    /**
     * 添加 session
     *
     * @param key
     */
    public static void add(String key, WebSocketSession session) {
        // 添加 session
        SESSION_POOL.put(key, session);
    }

    /**
     * 删除 session,会返回删除的 session
     *
     * @param key
     * @return
     */
    public static WebSocketSession remove(String key) {
        // 删除 session
        return SESSION_POOL.remove(key);
    }

    /**
     * 删除并同步关闭连接
     *
     * @param key
     */
    public static void removeAndClose(String key) {
        WebSocketSession session = remove(key);
        if (session != null) {
            try {
                // 关闭连接
                session.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获得 session
     *
     * @param key
     * @return
     */
    public static WebSocketSession get(String key) {
        // 获得 session
        return SESSION_POOL.get(key);
    }


    /**
     * 发送全部消息
     *
     * @param message
     */
    public void sendMessage(WsMessage message) {
        Map<String, Object> params = MapUtil.of();
        params.put("userId", "");
        params.put("message", JsonUtils.objToJson(message));
        params.put(RedisKeyConstants.HANDLER_NAME, REDIS_TOPIC_NAME);
        SpringContextUtils.getBean(RedisUtils.class).getRedisTemplate().convertAndSend(RedisKeyConstants.REDIS_TOPIC_NAME, params);
    }


    /**
     * 发送信息给指定用户
     * 通过redis广播 支持ws集群
     *
     * @param message
     */
    public void sendMessage(String userId, WsMessage message) {
        log.info("【websocket消息】广播消息:" + message);
        Map<String, Object> params = MapUtil.of();
        params.put("userId", userId);
        params.put("message", JsonUtils.objToJson(message));
        params.put(RedisKeyConstants.HANDLER_NAME, REDIS_TOPIC_NAME);
        SpringContextUtils.getBean(RedisUtils.class).getRedisTemplate().convertAndSend(RedisKeyConstants.REDIS_TOPIC_NAME, params);
    }

    /**
     * 此为单点消息(多人)
     *
     * @param userIds
     * @param message
     */
    public void sendMessage(String[] userIds, WsMessage message) {
        for (String userId : userIds) {
            sendMessage(userId, message);
        }
    }


    /**
     * 服务端推送消息
     *
     * @param userId
     * @param message
     */
    public void pushMessage(String userId, TextMessage message) {
        WebSocketSession session = SESSION_POOL.get(userId);
        if (session != null && session.isOpen()) {
            try {
                synchronized (session) {
                    log.info("【websocket消息】 单点消息:" + message);
                    session.sendMessage(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 服务器端推送消息
     */
    public void pushMessage(TextMessage message) {
        SESSION_POOL.values().forEach(session -> {
            try {
                session.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


}
