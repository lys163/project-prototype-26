package com.picturebook.global.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Pattern;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private static final String MASKED = "***";

    private static final Pattern SYNTHETIC_NAME = Pattern.compile("^arg\\d+$");

    private static final Set<String> SENSITIVE_NAMES = Set.of(
            "password", "pwd", "secret", "token", "accesstoken",
            "refreshtoken", "authorization", "credential", "cardnumber",
            "ssn", "pin"
    );

    // ── Pointcut 정의 ──

    @Pointcut("within(com.picturebook..controller..*)")
    public void controllerLayer() {}

    @Pointcut("within(com.picturebook..service..*)")
    public void serviceLayer() {}

    @Pointcut("within(com.picturebook..repository..*)")
    public void repositoryLayer() {}

    // ── Controller 로깅: 요청/응답 + 실행 시간 ──

    @Around("controllerLayer()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String args = buildSafeArgs(joinPoint);

        log.info("[Controller] {}.{}() 호출 | args={}", className, methodName, args);

        long start = System.currentTimeMillis();
        String outcome = "완료";
        try {
            return joinPoint.proceed();
        } catch (Throwable t) {
            outcome = "실패";
            throw t;
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            log.info("[Controller] {}.{}() {} | 실행시간={}ms", className, methodName, outcome, elapsed);
        }
    }

    // ── Service 로깅: 메서드 실행 추적 ──

    @Around("serviceLayer()")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.debug("[Service] {}.{}() 호출 | args={}", className, methodName,
                buildSafeArgs(joinPoint));

        long start = System.currentTimeMillis();
        String outcome = "완료";
        try {
            return joinPoint.proceed();
        } catch (Throwable t) {
            outcome = "실패";
            throw t;
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            log.debug("[Service] {}.{}() {} | 실행시간={}ms", className, methodName, outcome, elapsed);
        }
    }

    // ── 예외 로깅: 모든 레이어 공통 ──

    @AfterThrowing(pointcut = "controllerLayer() || serviceLayer() || repositoryLayer()",
            throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.error("[Exception] {}.{}() 에서 예외 발생 | type={} | message={}",
                className, methodName,
                ex.getClass().getSimpleName(), ex.getMessage(), ex);
    }

    // ── 민감 데이터 마스킹 ──

    private String buildSafeArgs(JoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            Annotation[][] paramAnnotations = signature.getMethod().getParameterAnnotations();

            if (args.length == 0) {
                return "[]";
            }

            StringJoiner joiner = new StringJoiner(", ", "[", "]");
            for (int i = 0; i < args.length; i++) {
                String name = (paramNames != null) ? paramNames[i] : "arg" + i;
                if (shouldMask(name, paramAnnotations[i])) {
                    joiner.add(name + "=" + MASKED);
                } else {
                    joiner.add(name + "=" + args[i]);
                }
            }
            return joiner.toString();
        } catch (Exception e) {
            return "[args 파싱 실패 - 마스킹 처리됨]";
        }
    }

    private boolean shouldMask(String name, Annotation[] annotations) {
        if (SYNTHETIC_NAME.matcher(name).matches()) {
            return true;
        }
        if (isSensitiveName(name)) {
            return true;
        }
        return hasNoLogAnnotation(annotations);
    }

    private boolean hasNoLogAnnotation(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof NoLog) {
                return true;
            }
        }
        return false;
    }

    private boolean isSensitiveName(String name) {
        return SENSITIVE_NAMES.contains(name.toLowerCase());
    }
}
