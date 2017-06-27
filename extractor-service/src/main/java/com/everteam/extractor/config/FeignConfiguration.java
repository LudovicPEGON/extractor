package com.everteam.extractor.config;

import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.everteam.extractor")
public class FeignConfiguration {

}
