# Project: jasper-1

## Overview

This repository contains a Spring Boot project named "jasper-1". It serves as a demonstration project for integrating JasperReports with a Spring Boot application.

## Key Features and Technologies

*   **Java Version:** 17
*   **Framework:** Spring Boot
    *   Spring Data JPA: For data persistence.
    *   Spring Web: For building web applications and RESTful APIs.
    *   Spring Security: For authentication and authorization.
*   **Reporting:** JasperReports (version 6.20.0) is used for generating reports.
*   **Database:**
    *   Supports Oracle database connectivity (via `ojdbc11` driver).
    *   Uses H2 in-memory database for testing purposes.
*   **Development Tools:**
    *   Lombok: To reduce boilerplate code.
    *   Spring Boot Devtools: For enhanced development-time experience.

## Security Configuration

The application is configured as an OAuth2 Resource Server.
*   All incoming requests require authentication.
*   Authentication is handled via JSON Web Tokens (JWTs).
*   CSRF (Cross-Site Request Forgery) protection is disabled, which is a common practice for stateless, token-based authentication systems.

## Build Tool

*   Maven is used as the build and dependency management tool.

## Audit Logging with Kafka and AOP

This project implements audit logging for specific service methods using Aspect-Oriented Programming (AOP) and Kafka.
A custom annotation, `@Auditable`, can be applied to any method to enable auditing for it.

### How it Works

1.  **`@Auditable` Annotation:** Methods annotated with `@com.jules.project.aop.annotation.Auditable` will be intercepted by an AOP aspect.
2.  **AOP Aspect (`AuditAspect.java`):**
    *   Captures information about the method call, including:
        *   Fully qualified method name.
        *   Authenticated username (from Spring Security context).
        *   Timestamp of the call.
        *   Execution time of the method.
    *   Sends this information as a JSON message to a Kafka topic.
3.  **Kafka Topic:**
    *   The default topic is `audit-log-topic` (configurable in `application.properties` via `audit.kafka.topic`).
4.  **Kafka Consumer (`AuditLogConsumer.java`):**
    *   Listens to the specified Kafka topic.
    *   Logs the received audit messages using SLF4J.
    *   **Note:** Currently, the consumer only logs the messages. Database persistence for audit logs is planned for future development (see `TODO` in `AuditLogConsumer.java`).

### Local Kafka Setup (for Development)

The project includes a `docker-compose.yml` file to easily run Kafka and Zookeeper locally.

1.  **Prerequisites:** Docker and Docker Compose must be installed.
2.  **Start Kafka:**
    Open a terminal in the project root directory and run:
    ```bash
    docker-compose up -d
    ```
    This will start Kafka (listening on `localhost:9092`) and Zookeeper in detached mode.
3.  **Stop Kafka:**
    To stop the services, run:
    ```bash
    docker-compose down
    ```

### Using `@Auditable`

To audit a method, simply add the `@Auditable` annotation above its declaration:

```java
import com.jules.project.aop.annotation.Auditable;

// ...

public class MyService {

    @Auditable
    public void myAuditedMethod(String parameter) {
        // ... business logic ...
    }
}
```
