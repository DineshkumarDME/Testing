package com.jules.project.aop.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper; // Or any other JSON library you prefer
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.lang.reflect.Method;
import java.time.Instant;

@Aspect
@Component
public class AuditAspect {

	private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@Value("${audit.kafka.topic}")
	private String auditTopic;

	@Autowired
	private ObjectMapper objectMapper; // For creating JSON strings

	@Around("@annotation(com.jules.project.aop.annotation.Auditable)")
	public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
		long startTime = System.currentTimeMillis();
		Object result;
		try {
			result = joinPoint.proceed();
		} finally {
			long duration = System.currentTimeMillis() - startTime;
			try {
				MethodSignature signature = (MethodSignature) joinPoint.getSignature();
				Method method = signature.getMethod();
				String methodName = joinPoint.getSignature().getDeclaringTypeName() + "." + method.getName();
				String userName = "anonymousUser"; // Default if no authentication
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				if (authentication != null && authentication.isAuthenticated()) {
					userName = authentication.getName();
				}

				ObjectNode auditData = objectMapper.createObjectNode();
				auditData.put("timestamp", Instant.now().toString());
				auditData.put("userName", userName);
				auditData.put("methodName", methodName);
				auditData.put("executionTimeMs", duration);
				// Add more details if needed, e.g., method arguments
				// For example: auditData.put("arguments",
				// objectMapper.writeValueAsString(joinPoint.getArgs()));

				String auditMessage = objectMapper.writeValueAsString(auditData);
				kafkaTemplate.send(auditTopic, auditMessage);
				log.info("Audit log sent for method: {}", methodName);

			} catch (Exception e) {
				log.error("Error sending audit log to Kafka for method: {}", joinPoint.getSignature().getName(), e);
				// Decide if the original exception should be rethrown or if this audit failure
				// is critical
			}
		}
		return result;
	}
}
