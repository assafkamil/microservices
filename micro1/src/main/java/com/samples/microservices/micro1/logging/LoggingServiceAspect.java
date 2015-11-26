package com.samples.microservices.micro1.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class LoggingServiceAspect {
    private static final Logger logger = LoggerFactory
            .getLogger(LoggingServiceAspect.class);

    @AfterThrowing(pointcut = "execution(* com.samples..*.*(*))",
                    throwing = "ex")
    public void logException(JoinPoint joinPoint, Exception ex) {
        String msg = String.format("Method failed %s with params",
                joinPoint.getSignature().getName());
        logger.error(msg, ex);
    }
}
