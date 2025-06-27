# Customer Management API – Project Overview


### Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot
- **Build Tool**: Maven
- **Database**: PostgreSQL (via Docker)
- **Observability**: Logstash + Elasticsearch + Kibana (ELK Stack)
- **Containerization**: Docker
- **Orchestration**: Kubernetes (Minikube)
- **API Documentation**: Swagger/OpenAPI 3
- **Testing**: JUnit 5, Mockito, MockMvc, H2 Database for Integration Tests
- **Client App**: Python3

### Requirements (for macOS with Apple Silicon)

To run and test this project locally, ensure the following tools are installed:

Development Tools:
- **Java 21** – For building and running the Spring Boot application
- **Maven** – For dependency management and build automation
- **Python 3** – For running the API integration test client

Containerization & Orchestration:
- **Docker Desktop** – For containerization and managing Docker Compose setup
- **Minikube** – For running Kubernetes locally

Development Environment:
- **IntelliJ IDEA** – Preferred IDE for developing and debugging the project
- **Postman** – For manual API testing

### How to Run the Project

Access Application via:

- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **Observe via Kibana Dashboard**: [http://localhost:5601](http://localhost:5601)

### Locally (Must start PostgreSQL Container or else start everything with docker compose):

```bash
./mvnw spring-boot:run
```

### Run with Docker Compose:

To Start:
```bash
docker compose up --build -d
```

To Stop:

```bash
docker compose stop
```

### Deploy to Kubernetes (Minikube):

Configure Docker to point to Minikube. This step allows Docker to build images that are directly accessible to Minikube’s internal container runtime.


To set:
```bash
eval $(minikube docker-env)
```

To reset:
```bash
eval $(minikube docker-env --unset)
```

To start everything in K8s:
```bash
kubectl apply -f k8s --recursive
```

# Step 1: Build the API

### Architecture

- Follows a 3-layered architecture:
    - **Controller layer**: Handles HTTP requests and responses
    - **Service layer**: Contains business logic and validation
    - **Repository layer**: Interacts with the database via JPA
- Uses DTOs to decouple API contracts from entity models
- Input validation 
- Exception handling is centralized
- Swagger for API Documentation
- Aspect oriented programming for efficient logging
- Unit and Integration Tests (with H2 db)

### Endpoints

| Method | Endpoint                       | Description                           |
|--------|--------------------------------|---------------------------------------|
| GET    | `/api/customers`               | Get all customers                     |
| GET    | `/api/customers/{id}`          | Get a customer by ID                  |
| GET    | `/api/customers/search`        | Get customer by email address         |
| POST   | `/api/customers`               | Create a new customer                 |
| PUT    | `/api/customers/{id}`          | Update full customer details          |
| PATCH  | `/api/customers/{id}/contact`  | Update only contact number            |
| DELETE | `/api/customers/{id}`          | Delete a customer                     |
| HEAD   | `/api/customers/{id}`          | Check if customer exists by ID        |
| OPTIONS| `/api/customers`               | List supported HTTP methods           |

### Validation

- `givenName`, `familyName`, `emailAddress`, `contactNumber` are **mandatory**
- `middleName` is optional
- `emailAddress` must be valid, lowercase, and trimmed
- `contactNumber` must match the regex: `^\\+?[1-9][0-9]{6,14}$`

---

# Step 2: Integration and Acceptance Testing

The API implements a multi-layered testing strategy:

- **Unit Tests**: Focused on isolated business logic using `JUnit` and `Mockito`.
- **Integration Tests**: Verifies end-to-end flow between components (service, repository) using real database interactions (`H2` in-memory).
- **Acceptance Tests**: Verifies complete business scenarios and expected user interactions through API endpoints.

Test classes are cleanly organized to separate logic-level tests from full application behavior validation.

---

### How to Run

Run all tests (unit, integration, and acceptance):
```bash
mvn test
```

Run only acceptance tests for nightly builds:
```bash
mvn test -Dgroups=acceptance
```

# Step 3: Observability and Instrumentation

To ensure operational visibility and troubleshootability, observability has been integrated into the API using structured logging and the ELK Stack (Elasticsearch, Logstash, Kibana).

### Logging Strategy

The application uses **SLF4J with Logback** and emits structured JSON logs for every key layer:

- **Controller**, **Service**, and **Repository** interactions are logged using **Spring AOP**
- **Custom exceptions** and unexpected errors are logged using a centralized `@RestControllerAdvice`
- Logs include timestamps, request/response details, log level, and error messages (if any)
- 
### ELK Stack Integration

The logs are forwarded to an **ELK Stack** configured using Docker Compose:

- **Elasticsearch** stores and indexes the logs
- **Logstash** receives structured logs over TCP and forwards them to Elasticsearch
- **Kibana** is used to visualize logs, filter by API paths, status codes, and exceptions

### Log Forwarding

A `SocketAppender` is configured in `logback-spring.xml` to stream logs to Logstash on port `5001` (mac doesnt allow 5000).


## Step 4: Containerization

The application is containerized using **Docker** to ensure consistent builds and deployments across different environments.

### Approach

- A lightweight base image is used to run the Spring Boot JAR file.
- The Maven build process compiles the project and produces an executable JAR.
- This JAR is run inside the container.

### Configuration

The following inputs are required when running the container:

- **Database URL**: JDBC URL pointing to the PostgreSQL instance.
- **Username** and **Password**: Credentials for the PostgreSQL database.

# Step 5: Kubernetes Deployment

The application is deployed to Kubernetes using declarative YAML configurations and tested on a local Minikube cluster. This setup can be easily extended to cloud environments.

### Overview

- The customer API and PostgreSQL are each defined with their own deployment and service configurations.
- The API is exposed externally using a NodePort or optionally via an Ingress.
- PersistentVolumeClaims are used for PostgreSQL to maintain data across restarts.

### How to Deploy

- Start Minikube.
- Apply all Kubernetes YAML files using `kubectl apply -f k8s/`.
- Verify that pods, services, and volumes are running using `kubectl get all`.
- Port forward to get access locally with `kubectl port-forward service/customer-api 8080:8080 &
`.
- Access the API at `http://<minikube-ip>:30080` (or via Ingress if configured).

### Key Configurations

- Environment variables for the API are injected via the deployment YAML (e.g., database URL, credentials).
- PostgreSQL is configured with persistent storage and basic authentication.
- Services are defined to allow internal and external communication.

# Step 6: CI/CD Pipeline (Conceptual, actual testing not done)

A CI/CD pipeline is designed to automate the build, test, and deployment lifecycle of the customer API using GitHub Actions.

### CI Pipeline (`.github/workflows/ci.yml`)

The CI pipeline performs the following tasks:

- Builds the application using Maven
- Runs unit tests
- Optionally runs **acceptance tests** based on the `RUN_ACCEPTANCE` environment variable

This allows dynamic control of test stages based on branch or input configuration. For example, by intentionally setting a branch name incorrectly or toggling `RUN_ACCEPTANCE`, teams can selectively skip acceptance tests on development branches.

### CD Pipeline (`.github/workflows/cd.yml`)

The CD pipeline supports gated deployments across the following environments:

- **Dev**: Automatically deployed after a successful build
- **QA**: Requires manual approval before promotion
- **Prod**: Requires a final approval gate before deployment

This approach ensures safe and deliberate progression of code from development to production.

### Pipeline Features

1. **Build**
   - Triggered on every push or pull request to the `main` branch.
   - Uses a Maven build to compile and package the application into a JAR file.

2. **Unit & Integration Tests**
   - Runs all JUnit-based unit tests.
   - Executes integration and acceptance tests using an in-memory H2 database.
   - Fails the pipeline if any test fails.

3. **Containerization**
   - Builds a Docker image tagged as `customer-api:latest`.
   - Optionally pushes the image to Docker Hub or GitHub Container Registry.

4. **Deployment**
   - On merge to `main`, deploys the application to a local Minikube cluster or staging environment using `kubectl apply -f k8s/`.
   - Can be gated manually via GitHub Actions approval for production rollouts.

5. **Manual & Automated Gates**
   - Pull request validation includes automated linting, formatting checks, and test runs.
   - Manual approval is required for production deployment.
   - Environment secrets (e.g., registry credentials) are stored in GitHub Secrets.

6. **Extensibility in future**
   - Can integrate SonarQube for code quality checks.
   - Extendable to cloud CI/CD platforms like Jenkins, GitLab CI, or ArgoCD for production pipelines.


## Step 7: App Integration

To demonstrate API contract adherence and integration, a **Python CLI script** was developed using the `requests` library. This client interacts with the RESTful API created in Step 1.

### Solid Contract Testing

- The client sends predefined payloads and asserts expected schema in responses.
- Invalid inputs (e.g., malformed emails, missing required fields) are tested to verify validation and error handling.
- Each test prints clear **pass/fail** output to the terminal, acting as lightweight **acceptance verification**.

### How to Run

1. Ensure the API is running (e.g., via Docker or Minikube).
2. Update the base URL in the Python script if needed (default: `http://localhost:8080`).
3. Execute the script:

```bash
pip insatll requests
python3 api_client.py
```



