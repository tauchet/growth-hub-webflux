package com.pragmaticos.transactions.infrastructure.configuration.logger;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggerAdvice {



    Logger logger = LoggerFactory.getLogger(LoggerAdvice.class);

    @Pointcut(value = "execution(* com.pragmaticos.transactions..*(..))")
    public void myPointcut() {
    }

    @Before("execution(* com.pragmaticos.transactions.adapters.driven.mongoadapter.service.UserService.getById(..))")
    public void checkGetUserById(JoinPoint joinPoint) {
        logger.info("Se ha intentado buscar el usuario: " + joinPoint.getArgs()[0]);
    }

    @Around("myPointcut()")
    public Object applicationLogger(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        String methodName = proceedingJoinPoint.getSignature().getName();
        String className = proceedingJoinPoint.getTarget().getClass().toString();
        Object[] args = proceedingJoinPoint.getArgs();
        logger.info("[CHECK] {} {} {}", className, methodName, Arrays.toString(args));
        Object reply = proceedingJoinPoint.proceed();

        String str = String.valueOf(reply);
        str = str.replaceAll("username=[^&]+", "username=***");

        logger.info("[FINISH] {} {} {}", className, methodName, str);
        return reply;
    }

}
