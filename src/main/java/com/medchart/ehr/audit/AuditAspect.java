package com.medchart.ehr.audit;

import com.medchart.ehr.domain.auth.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;

    @Around("@annotation(com.medchart.ehr.audit.AuditAccess)")
    public Object auditAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AuditAccess auditAccess = method.getAnnotation(AuditAccess.class);

        Long patientId = extractPatientId(signature.getParameterNames(), joinPoint.getArgs(),
                auditAccess.resourceType());
        Long resourceId = patientId == null
                ? extractResourceId(signature.getParameterNames(), joinPoint.getArgs(), auditAccess.resourceType())
                : null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = getCurrentUserId(authentication);

        AuditEvent.AuditEventBuilder eventBuilder = AuditEvent.builder()
                .userId(userId)
                .userName(getCurrentUserName(authentication))
                .patientId(patientId)
                .resourceId(resourceId)
                .action(auditAccess.action())
                .resourceType(auditAccess.resourceType())
                .description(auditAccess.description())
                .ipAddress(getClientIpAddress())
                .userAgent(getUserAgent());

        try {
            Object result = joinPoint.proceed();
            eventBuilder.success(true);
            auditService.saveAuditEventAsync(eventBuilder.build());
            return result;
        } catch (Exception e) {
            eventBuilder.success(false);
            eventBuilder.errorMessage(e.getMessage());
            auditService.saveAuditEventAsync(eventBuilder.build());
            throw e;
        }
    }

    private Long extractPatientId(String[] parameterNames, Object[] args, String resourceType) {
        if (parameterNames != null) {
            for (int i = 0; i < args.length && i < parameterNames.length; i++) {
                if ("patientId".equals(parameterNames[i]) && args[i] instanceof Long) {
                    return (Long) args[i];
                }
            }
        }
        return "Patient".equals(resourceType) ? firstLongArg(args) : null;
    }

    private Long extractResourceId(String[] parameterNames, Object[] args, String resourceType) {
        if (parameterNames == null) {
            return null;
        }
        String typedIdName = resourceType.isEmpty()
                ? null
                : Character.toLowerCase(resourceType.charAt(0)) + resourceType.substring(1) + "Id";
        for (int i = 0; i < args.length && i < parameterNames.length; i++) {
            String name = parameterNames[i];
            if (args[i] instanceof Long && ("id".equals(name) || name.equals(typedIdName))) {
                return (Long) args[i];
            }
        }
        return null;
    }

    private Long firstLongArg(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }
        return null;
    }

    private String getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User && ((User) principal).getId() != null) {
            return String.valueOf(((User) principal).getId());
        }
        return authentication.getName();
    }

    private String getCurrentUserName(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }
        return authentication.getName();
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Could not get client IP", e);
        }
        return null;
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return attrs.getRequest().getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.debug("Could not get user agent", e);
        }
        return null;
    }
}
