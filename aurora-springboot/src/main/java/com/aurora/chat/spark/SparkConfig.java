package com.aurora.chat.spark;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Component
@Configuration
public class SparkConfig {
    
    @Value("${xf-spark.appId}")
    private String appId;
    
    @Value("${xf-spark.apiSecret}")
    private String apiSecret;
    
    @Value("${xf-spark.apiKey}")
    private String apiKey;
    
    @Value("${xf-spark.hostUrl}")
    private String hostUrl;
    
    @Value("${xf-spark.maxResponseTime}")
    private Integer maxResponseTime;
}
