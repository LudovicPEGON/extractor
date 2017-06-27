package com.everteam.extractor.client;

import java.io.IOException;
import java.net.MalformedURLException;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.everteam.extractor.domain.TextData;

import feign.codec.Encoder;

@FeignClient(name = "extractor")
public interface TextClient {

    @Configuration
    public class MultipartSupportConfig {
        @Bean
        @Primary
        @Scope("prototype")
        public Encoder encoder() {
            return new FeignSpringFormEncoder();
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "api/text/uri", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public TextData getText(@RequestParam(value = "uri") String uri) throws MalformedURLException, IOException;

    @RequestMapping(method = RequestMethod.POST, value = "api/text/binary", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TextData getText(@RequestPart(value = "multipartFile") MultipartFile multipartFile) throws IOException;
}
