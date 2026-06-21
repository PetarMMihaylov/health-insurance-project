# 🏥 Health Insurance Application

## 📌 Overview
The Health Insurance Application is a Spring Boot–based web application designed to manage health insurance policies, clients, and claims.  
It provides secure authentication, policy management and claims management.

Disclaimer: This project was initially created for Spring-Advanced-October 2025 course and has been slightly changed (integration has been removed) to meet requirements for Spring-Fundamentals-June 2026.

You can start the application on localhost:8080

---

## 🛠 Tech Stack
- **Language:** Java 17
- **Framework:** Spring Boot 3.4.0
- **Frontend:** Thymeleaf + HTML/CSS
- **Database:** MySQL (production), H2 (in-memory for testing)
- **Build Tool:** Maven
- **Security:** Spring Security 
- **Other Tools:** Lombok, Spring Boot Actuator, Spring Boot DevTools
- **Testing:** Unit and integration tests

---

## ✅ Features
- User authentication and role-based and permission-based authorization
- Policy management (choose, update, view insurance policies)
- Client management (register, update client details)
- Claims creation using a selection of predefined medical document templates, located in static/documents
- Claims processing and validation
- Automatic scheduled claims evaluation
- Transaction management (view and delete)
- Secure password encryption with Spring Security
- Caching for performance optimization
- Health monitoring and metrics via Spring Boot Actuator
- Validation for user input and forms

---

## ⚙️ Functionalities
- **Data Persistence:** Managed via Spring Data JPA
- **Web Layer:** RESTful APIs and Thymeleaf templates
- **Security Layer:** Login, session management, and role-based access

---

## 🔗 Integrations
- **Databases:** MySQL (main), H2 (in-memory for testing)
- **Spring Ecosystem:** Actuator for monitoring, Validation for input checks
- **Testing:** JUnit + Spring Boot Starter Test + Spring Security Test
