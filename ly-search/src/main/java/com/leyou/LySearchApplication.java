package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 〈一句话功能简述〉<br>
 *
 * @author Administrator
 * @create 2018/12/7
 * @since 1.0.0
 */
@EnableFeignClients
@SpringBootApplication
@EnableDiscoveryClient
public class LySearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(LySearchApplication.class, args);
    }
}
