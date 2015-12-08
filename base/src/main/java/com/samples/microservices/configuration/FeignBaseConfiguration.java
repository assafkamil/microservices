package com.samples.microservices.configuration;

import com.samples.microservices.exceptions.ExceptionScanner;
import com.samples.microservices.feign.ErrorDecoderImp;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FeignBaseConfiguration {
    private Map<Integer, Class> exceptionMap = new HashMap<>();

    @PostConstruct
    public void wireExceptions() throws ClassNotFoundException {
        ExceptionScanner exceptionScanner = new ExceptionScanner();
        String packageName = this.getClass().getPackage().getName();
        Set<BeanDefinition> beanDefinitions = exceptionScanner.findCandidateComponents(packageName);
        for(BeanDefinition beanDefinition : beanDefinitions) {
            System.out.println(beanDefinition.getBeanClassName());

            Class cls = Class.forName(beanDefinition.getBeanClassName());
            ResponseStatus responseStatus = (ResponseStatus)cls.getAnnotation(ResponseStatus.class);
            if(responseStatus != null) {
                exceptionMap.put(responseStatus.value().value(), cls);
                System.out.println(responseStatus.value());
            }
        }
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new ErrorDecoderImp(exceptionMap);
    }
}
