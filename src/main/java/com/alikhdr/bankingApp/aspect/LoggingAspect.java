package com.alikhdr.bankingApp.aspect; // Changed package to 'aspect'

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Pointcut that matches all methods in classes annotated with @Service.
     */
    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceMethods() {
        // Method is empty as this is just a Pointcut, the implementations are in the advices.
    }

    /**
     * Advice that logs when a method is entered.
     * @param joinPoint join point for advice
     */
    @Before("serviceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        log.info("Entering method: {}.{}() with arguments[s] = {}",
            joinPoint.getSignature().getDeclaringTypeName(),
            joinPoint.getSignature().getName(),
            Arrays.toString(joinPoint.getArgs()));
    }

    /**
     * Advice that logs when a method is exited successfully.
     * @param joinPoint join point for advice
     * @param result result of the method execution
     */
    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("Exiting method: {}.{}() with result = {}",
            joinPoint.getSignature().getDeclaringTypeName(),
            joinPoint.getSignature().getName(),
            result);
    }
}
