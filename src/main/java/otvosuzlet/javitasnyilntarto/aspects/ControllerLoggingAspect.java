package otvosuzlet.javitasnyilntarto.aspects;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Aspect
@Component
public class ControllerLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger("fileLogger");
    
    @Before("within(otvosuzlet.javitasnyilntarto.controllers..*)")
    public void logClientIp(JoinPoint joinPoint) {
        HttpServletRequest request = getCurrentHttpRequest();

        String ipAddress = (request != null) ? request.getRemoteAddr() : "unknown";
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();

        String httpMethod = getHttpMethod(method);
        String path = getRequestPath(method);

        logger.info("Incoming {} request to '{}' from IP: {}", httpMethod, path, ipAddress);
    }

    private HttpServletRequest getCurrentHttpRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        return null;
    }

    private String getHttpMethod(Method method) {
        if (method.isAnnotationPresent(GetMapping.class)) return "GET";
        if (method.isAnnotationPresent(PostMapping.class)) return "POST";
        if (method.isAnnotationPresent(PatchMapping.class)) return "PATCH";
        if (method.isAnnotationPresent(PutMapping.class)) return "PUT";
        if (method.isAnnotationPresent(DeleteMapping.class)) return "DELETE";
        return "UNKNOWN";
    }

    private String getRequestPath(Method method) {
        Annotation[] annotations = method.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof GetMapping) {
                return ((GetMapping) annotation).value().length > 0 ? ((GetMapping) annotation).value()[0] : "";
            }
            if (annotation instanceof PostMapping) {
                return ((PostMapping) annotation).value().length > 0 ? ((PostMapping) annotation).value()[0] : "";
            }
            if (annotation instanceof PatchMapping) {
                return ((PatchMapping) annotation).value().length > 0 ? ((PatchMapping) annotation).value()[0] : "";
            }
            if (annotation instanceof PutMapping) {
                return ((PutMapping) annotation).value().length > 0 ? ((PutMapping) annotation).value()[0] : "";
            }
            if (annotation instanceof DeleteMapping) {
                return ((DeleteMapping) annotation).value().length > 0 ? ((DeleteMapping) annotation).value()[0] : "";
            }
        }
        return "";
    }
}
