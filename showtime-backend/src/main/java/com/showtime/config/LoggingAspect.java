package com.showtime.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** AOP logging: logs the name and execution time of every service method (arguments are not logged: passwords!). */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.showtime.service..*(..))")
    public Object logServiceCalls(ProceedingJoinPoint pjp) throws Throwable {
        String name = pjp.getSignature().getDeclaringType().getSimpleName() + "." + pjp.getSignature().getName();
        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            log.info("[AOP] {} finished in {} ms", name, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable t) {
            log.warn("[AOP] {} threw {}: {}", name, t.getClass().getSimpleName(), t.getMessage());
            throw t;
        }
    }
}
