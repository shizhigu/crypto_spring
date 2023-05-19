package com.example.CryptoSpring.configuration;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;


import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class LogAspect {
    /**
     * @After: Finally
     * @AfterReturning
     * @AtherThrowing
     */

    public String coloredText(String text, String code) {
        String ANSI_RESET = "\u001B[0m";
        return code + text + ANSI_RESET;
    }
    @Around("execution(public * com.example.CryptoSpring.controller.CryptoController.*(..))")
    public Object aroundMethod(ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
        String[] paramNames = codeSignature.getParameterNames();
        Map<String, Object> mapArgs = new HashMap<String, Object>();
        for (int i = 0; i < paramNames.length; i++) {
            mapArgs.put(paramNames[i], args[i]);
        }
        String argsString = mapArgs.toString();

        Object res;
        String ANSI_GREEN = "\u001B[32m";
        String ANSI_CYAN = "\u001B[36m";
        try {
            System.out.println("In the Around method AOP.");
            System.out.println("Method name: " + coloredText(methodName, ANSI_GREEN) + " with arguments: " + coloredText(argsString, ANSI_CYAN) + "\n");

            long k = System.currentTimeMillis();
            res = joinPoint.proceed();
            System.out.println(coloredText(methodName, ANSI_GREEN) + coloredText(argsString, ANSI_CYAN) + " method takes " + (System.currentTimeMillis() - k) + "ms.\n");
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return res;
    }

}
