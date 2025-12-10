# 🏥 Health Insurance Application

## 📌 Overview
The Health Insurance Application is a Spring Boot–based web application designed to manage health insurance policies, clients, and claims.  
It provides secure authentication, policy management, and integrations with external services for a complete insurance workflow.

---

## 🛠 Tech Stack
- **Language:** Java 17
- **Framework:** Spring Boot 3.4.0
- **Frontend:** Thymeleaf + HTML/CSS
- **Database:** MySQL (production), H2 (in-memory for testing)
- **Build Tool:** Maven
- **Security:** Spring Security 
- **Other Tools:** Lombok, Spring Boot Actuator, Spring Boot DevTools
- **API Integration:** Integration with reports-svc microservice
- **Testing:** Unit, API and integration tests

---

## ✅ Features
- User authentication and role-based and permission-based authorization
- Policy management (choose, update, view insurance policies)
- Client management (register, update client details)
- Claims processing and validation
- Transaction management (view and delete)
- Reports management (create, view and delete) via communication with Reports SVC microservice
- Secure password encryption with Spring Security
- Caching for performance optimization
- Health monitoring and metrics via Spring Boot Actuator
- Validation for user input and forms

---

## ⚙️ Functionalities
- **Data Persistence:** Managed via Spring Data JPA
- **Web Layer:** RESTful APIs and Thymeleaf templates
- **Security Layer:** Login, session management, and role-based access
- **Integration Layer:** OpenFeign for external API communication

---

## 🔗 Integrations
- **Databases:** MySQL (main), H2 (in-memory for testing)
- **External APIs:** Feign clients for third-party services
- **Spring Ecosystem:** Actuator for monitoring, Validation for input checks
- **Testing:** JUnit + Spring Boot Starter Test + Spring Security Test
