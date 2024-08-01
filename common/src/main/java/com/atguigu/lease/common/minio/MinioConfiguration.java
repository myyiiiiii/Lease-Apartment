package com.atguigu.lease.common.minio;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
@ConfigurationPropertiesScan("com.atguigu.lease.common.minio")
public class MinioConfiguration {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Autowired
    private MinioProperties minioProperties;
    @Bean
    public MinioClient minioClient(){
       return MinioClient.builder().endpoint(endpoint)
                .credentials(minioProperties.getAccessKey(),minioProperties.getSecretKey())
                .build();
    }

}
