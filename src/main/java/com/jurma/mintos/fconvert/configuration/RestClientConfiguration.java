package com.jurma.mintos.fconvert.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Configuration
@Slf4j
public class RestClientConfiguration {
    @Bean
    @Description("Exchange rate api web client configuration")
    public RestClient restClient(@Value("${exchange.rate.service.url}") String serviceUrl,
                                 @Value("${exchange.rate.service.accessKey}") String accessKey) {
        URI uri = UriComponentsBuilder.fromHttpUrl(serviceUrl).queryParam("access_key", accessKey).build().toUri();

        log.info("Exchange rate URL set to {}", serviceUrl);
        return RestClient.builder()
                .baseUrl(uri.toString())
                .build();
    }
}
