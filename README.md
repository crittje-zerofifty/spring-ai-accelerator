# Spring AI Accelerator

A super modular Spring AI project designed to jumpstart your LLM-based applications.

## 1. Ollama Installation

To run Large Language Models locally, we recommend using **Ollama**.

### Why Not Docker?
While you can run Ollama via Docker, we generally recommend a **native installation**. Running an LLM inside a Docker container adds an extra virtualization layer that can impact performance, especially when it comes to GPU acceleration. For the best performance and lowest latency, install it directly on your host machine.

- **Native Installation**: Download from [ollama.com](https://ollama.com/) and follow the instructions for your OS.
- **Docker (Optional)**: If you still prefer Docker, you can use the official `ollama/ollama` image, but ensure you have the appropriate GPU drivers passed through.

---

## 2. Architecture: Hexagonal & DDD

This project follows **Hexagonal Architecture** (also known as Ports and Adapters) and **Domain-Driven Design (DDD)** principles to ensure high modularity, testability, and a clear separation of concerns.

### Core Principles
- **Dependency Inversion**: The core business logic (Application layer) is independent of external technologies (Infrastructure layer). Infrastructure depends on Application, never the other way around.
- **Ports & Adapters**: 
    - **Input Ports**: Interfaces that define how the outside world can interact with the application (e.g., `ChatHistoryPort`).
    - **Output Ports**: Interfaces that define what the application needs from the outside world (e.g., `LlmHistoryClientPort`).
    - **Adapters**: Implementations of these ports in the Infrastructure layer (e.g., REST Controllers for input, LLM Clients for output).
- **Usecases**: Specific implementations of business logic that coordinate the flow of data through the ports.
- **Spring Boot & Spring AI**: The project leverages **Spring Boot** for dependency injection and lifecycle management, and **Spring AI** to integrate with LLMs, using `ChatClient`, `Advisors`, and `VectorStore`.

### Layer Structure
- `application`: Contains the "inside" of the hexagon.
    - `port.input`: Inbound interfaces (the API of the application).
    - `port.output`: Outbound interfaces (the SPI of the application).
    - `usecase`: Implementation of the business logic.
- `infrastructure`: Contains the "outside" of the hexagon.
    - `adapter`: Implementations of output ports (e.g., Database, LLM, External APIs).
    - `controller`: Entry points for input ports (e.g., Web, CLI).
    - `config`: Wiring of the application using Spring configuration.

---

## 3. Modularity & Profiles

This application is built with modularity at its core. You can easily toggle features on or off using Spring Profiles.

### Profile Overview

| Profile | Description | When to use |
| :--- | :--- | :--- |
| `no-history` | Standard chat without memory. | Simple stateless requests. |
| `history` | Enables chat history (JDBC based). | When you need the LLM to remember previous context. |
| `auth-azure` | Enables Azure AD OAuth2 authentication. | Production or secured environments requiring user login. |
| `rag` | Enables Retrieval Augmented Generation. | When you want the LLM to use your own data/documents. |
| `ollama` | Configures Ollama as the AI provider. | Local development with Ollama. |
| `elk-monitoring` | Enables logging and monitoring via ELK stack. | For deep log analysis and observability. |
| `grafana-monitoring` | Enables Micrometer metrics for Grafana. | For real-time performance dashboards. |

### Switching Profiles
You can switch profiles in your `application.yaml` or via command line:
```bash
./gradlew bootRun --args='--spring.profiles.active=history,ollama,grafana-monitoring'
```

This example builds an application with **history** enabled. In addition, it uses **Ollama** as the AI provider and **Grafana** for monitoring.

---

## 3. Monitoring Environments

Setup your monitoring stack locally using the provided Docker Compose files in the `/monitoring` directory.

### ELK vs. Grafana: Which one to use?

While both provide observability, they serve different purposes in this project:

- **ELK Stack (Elasticsearch, Logstash, Kibana)**: 
  - **Focus**: Log Analysis. 
  - **Usage**: Use this when you need to search through high-volume logs, trace specific AI prompts, or debug complex errors. It captures the full context of what is happening in the application.
- **Grafana & Prometheus**: 
  - **Focus**: Metrics and Performance. 
  - **Usage**: Use this for real-time dashboards showing system health, request latency, and token usage. It’s better for seeing "at a glance" how the system is performing rather than digging into individual log lines.

### ELK Stack
1. Start the services:
   ```bash
   docker-compose -f monitoring/docker-compose-accelerator-elk-monitoring.yml up -d
   ```
2. Run the app with the profile: `elk-monitoring`.
3. **Access Kibana**: Open [http://localhost:5601](http://localhost:5601) in your browser.
   - Go to **Management > Stack Management > Index Patterns**.
   - Create an index pattern (e.g., `spring-ai-logs-*`) to start viewing logs in **Discover**.

### Grafana & Prometheus
1. Start the services:
   ```bash
   docker-compose -f monitoring/docker-compose-accelerator-grafana-monitoring.yml up -d
   ```
2. Run the app with the profile: `grafana-monitoring`.
3. **Access Dashboards**:
   - **Grafana**: [http://localhost:3000](http://localhost:3000) (Default login: `admin` / `admin`).
   - **Prometheus**: [http://localhost:9090](http://localhost:9090).
4. **Setup Grafana**:
   - Add Prometheus as a Data Source: `http://prometheus:9090`.
   - Import dashboards (e.g., Spring Boot Observability dashboards).

### Configuration for Hosted (Non-Local) Environments

If you are moving away from `localhost` to a hosted monitoring stack (e.g., Elastic Cloud, Managed Grafana, or a central company server), you need to update the connection points:

#### 1. Hosted ELK
- **Logstash Destination**: Update `src/main/resources/logback-spring.xml`. Change `<destination>localhost:5044</destination>` to your hosted Logstash endpoint.
- **Authentication**: If your hosted Elasticsearch requires authentication, update `application-elk-monitoring.yaml` with the appropriate `spring.elasticsearch.username` and `spring.elasticsearch.password`.

#### 2. Hosted Grafana/Prometheus
- **Prometheus Scrape Config**: In your hosted Prometheus configuration, ensure it can reach your application's actuator endpoint (e.g., `http://your-app-host:8080/actuator/prometheus`).
- **Grafana Data Source**: In your hosted Grafana, point the Prometheus data source to your hosted Prometheus URL.

---

## 4. Azure OAuth2 Configuration

To use Azure AD for authentication, you need to register an application in the Azure Portal and configure the following.

### Azure Setup Requirements
1. **App Registration**: Create a new registration in "App registrations".
2. **Redirect URI**: Set it to `http://localhost:8080/login/oauth2/code/azure`.
3. **Client Secret**: Generate a new client secret.
4. **API Permissions**: Ensure `openid`, `profile`, and `email` are granted.

### Configuration
Use the `auth-azure` profile and provide the following environment variables:

- `AZURE_CLIENT_ID`: Your Azure Application (client) ID.
- `AZURE_CLIENT_SECRET`: Your Azure Client Secret.

The issuer URI is pre-configured in `application-auth-azure.yaml`, but you may need to update the Tenant ID in the URL:
`https://login.microsoftonline.com/{your-tenant-id}/v2.0`

### How it works in practice

#### For Web Users
When a user accesses the service, they will be redirected to the Microsoft Login page. After successful authentication, a session is created. The user's email is extracted from the OIDC token and used to sandbox their chat history.

#### For REST/API Calls
Currently, the service is configured for **OAuth2 Login** (Authorization Code Flow). 
- **Browser-based**: Works automatically via session cookies after login.
- **Service-to-Service**: If you want to call this as a pure REST API with a `Bearer` token from a mobile app or another service, you would need to adjust the `AzureSecurityConfig` to support `oauth2ResourceServer`. 

When `auth-azure` is active, the `/chat` endpoint expects the user to be authenticated. If you call it without a valid session, you will receive a `401 Unauthorized` or be redirected to the login page.


