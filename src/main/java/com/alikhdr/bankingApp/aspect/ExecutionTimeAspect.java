package com.alikhdr.bankingApp.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExecutionTimeAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Advice that measures the execution time of methods in classes annotated with @RestController.
     * @param proceedingJoinPoint join point for advice
     * @return result of the method execution
     * @throws Throwable if the method throws an exception
     */
    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object measureExecutionTime(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed(); // Execute the method
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        log.info("Method {}.{}() executed in {}ms",
            proceedingJoinPoint.getSignature().getDeclaringTypeName(),
            proceedingJoinPoint.getSignature().getName(),
            executionTime);

        return result;
    }
}
