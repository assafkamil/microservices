package com.sample.logprocessor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class InitializerService implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private LogService logService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logService.processLog();
    }
}
