package com.assignment.backend.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("execution(* com.assignment.backend.unitTests.service..*(..)) || " +
            "execution(* com.assignment.backend.unitTests.controller..*(..)) || " +
            "execution(* com.assignment.backend.repository..*(..))")
    public void applicationLayer() {}

    @Before("applicationLayer()")
    public void logBefore(JoinPoint joinPoint) {
        log.info("Entering: {} with args: {}", joinPoint.getSignature(), Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(pointcut = "applicationLayer()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("Exiting: {} with result: {}", joinPoint.getSignature(), result);
    }

    @AfterThrowing(pointcut = "applicationLayer()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        if (isClientError(ex)) {
            log.warn("Validation error in {}: {}", joinPoint.getSignature(), ex.getMessage());
        } else {
            log.error("Exception in {}: {}", joinPoint.getSignature(), ex.getMessage(), ex);
        }
    }

    private boolean isClientError(Throwable ex) {
        return ex instanceof jakarta.validation.ConstraintViolationException ||
                ex instanceof org.springframework.web.bind.MethodArgumentNotValidException ||
                ex instanceof org.springframework.web.bind.MissingServletRequestParameterException ||
                ex instanceof org.springframework.web.bind.MissingPathVariableException ||
                ex instanceof org.springframework.web.bind.MissingRequestHeaderException ||
                ex instanceof com.assignment.backend.exception.DuplicateEmailException ||
                ex instanceof com.assignment.backend.exception.CustomerNotFoundException ||
                ex instanceof IllegalArgumentException;
    }
}