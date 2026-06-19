package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 AccessKey 配置类
 * 从 application.properties 中读取 aliyun.access-key-id 与 aliyun.access-key-secret
 */
@Configuration
@ConfigurationProperties(prefix = "aliyun")
public class AliyunConfig {

    /** 阿里云 AccessKey ID */
    private String accessKeyId;

    /** 阿里云 AccessKey Secret */
    private String accessKeySecret;

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }
}
