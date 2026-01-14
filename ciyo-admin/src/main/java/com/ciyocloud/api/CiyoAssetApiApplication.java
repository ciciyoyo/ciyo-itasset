package com.ciyocloud.api;

import com.ciyocloud.common.constant.CommonConstants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author codeck
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@ComponentScan("com.ciyocloud.**")
public class CiyoAssetApiApplication {

    public static void main(String[] args) {
/*
        // 启动 Redis 服务 演示模式快捷使用
        RedisServer redisServer = new RedisServer(6389);
        try {
            redisServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
*/

        SpringApplication.run(CiyoAssetApiApplication.class, args);
        System.out.printf("=============================================== >>> CIYO ASSET v%s 启动成功 >>>%n", CommonConstants.SYSTEM_VERSION);
    }

}
