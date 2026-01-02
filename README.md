# DietiEstates25 - Real Estate Management API

## Description
DietiEstates25 is a robust RESTful API designed for managing real estate operations. It provides comprehensive features for handling properties, users, agencies, and authentication. Built with Java and Spring Boot, it leverages modern practices for security and data management.

## Prerequisites
Before you begin, ensure you have the following installed on your system. These instructions are OS-agnostic (Windows, macOS, Linux).

*   **Java Development Kit (JDK) 17**: This project requires Java 17 or higher.
    *   Verify with: `java -version`
*   **Maven 3.8+**: Build automation tool.
    *   Verify with: `mvn -version`
*   **Docker** (Optional): If you prefer to run the application in a container.

## Getting Started

### 1. clone the Repository
```bash
git clone <repository-url>
cd dietiestates25
```

### 2. Configuration
The application comes pre-configured for a development environment using an **In-Memory H2 Database**. This means you **do not** need to install or configure an external database like PostgreSQL or MySQL to get started. The database is created when the app starts and destroyed when it stops.

**Default Database Credentials:**
*   **Database URL:** `jdbc:h2:mem:dietiestates`
*   **Username:** `sa`
*   **Password:** `password`

You can verify or change these settings in `src/main/resources/application.properties`.

---

## Running the Application

Choose one of the following methods to run the application.

### Option A: Using Maven (Recommended for Dev)
This method is the quickest for local development.

**Command (Cross-platform):**
```bash
mvn spring-boot:run
```

### Option B: Using an Executable JAR
This mimics a production deployment.

1.  **Build the application:**
    ```bash
    mvn clean package -DskipTests
    ```
2.  **Run the JAR file:**
    ```bash
    java -jar target/dietiestates25-0.0.1-SNAPSHOT.jar
    ```

### Option C: Using Docker
Run the application in an isolated container environment.

1.  **Build the Docker image:**
    ```bash
    docker build -t dietiestates25 .
    ```
2.  **Run the container:**
    ```bash
    docker run -p 8080:8080 dietiestates25
    ```

The application will start on port `8080`.

---

## Accessing the Application

Once the application is up and running, you can access the following resources:

### 1. API Documentation (Swagger UI)
Interactive API documentation is available via Swagger UI. You can test endpoints directly from the browser.
*   **URL:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### 2. H2 Database Console
A web-based interface to inspect the in-memory database.
*   **URL:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
*   **Login Details:**
    *   **Driver Class:** `org.h2.Driver`
    *   **JDBC URL:** `jdbc:h2:mem:dietiestates`
    *   **User Name:** `sa`
    *   **Password:** `password`

---

## Running Tests

To run the automated test suite (Unit and Integration tests):
```bash
mvn test
```

## Tech Stack

*   **Language:** Java 17
*   **Framework:** Spring Boot 3.2.1
*   **Database:** H2 (In-Memory), compatible with PostgreSQL
*   **Security:** Spring Security & JWT
*   **Documentation:** SpringDoc OpenApi 2.3.0
*   **Tools:** Lombok, Maven, Docker
