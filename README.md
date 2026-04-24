# Spring AI Accelerator

A super modular Spring AI project designed to jumpstart your LLM-based applications.

## 1. Ollama Installation

To run Large Language Models locally, we recommend using **Ollama**.

### Why Not Docker?
While you can run Ollama via Docker, we generally recommend a **native installation**. Running an LLM inside a Docker container adds an extra virtualization layer that can impact performance, especially when it comes to GPU acceleration. For the best performance and lowest latency, install it directly on your host machine.

- **Native Installation**: Download from [ollama.com](https://ollama.com/) and follow the instructions for your OS.
- **Docker (Optional)**: If you still prefer Docker, you can use the official `ollama/ollama` image, but ensure you have the appropriate GPU drivers passed through.

---

## 2. Modularity & Profiles

This application is built with modularity at its core. You can easily toggle features on or off using Spring Profiles.

### Profile Overview

| Profile | Description | When to use |
| :--- | :--- | :--- |
| `no-history` | Standard chat without memory. | Simple stateless requests. |
| `history` | Enables chat history (JDBC based). | When you need the LLM to remember previous context. |
| `auth-azure` | Enables Azure AD OAuth2 authentication. | Production or secured environments requiring user login. |
| `rag` | Enables Retrieval Augmented Generation. | When you want the LLM to use your own data/documents. |
| `secure-rag` | Enables Secure RAG with metadata filtering. | When you need to restrict document access based on user security levels. |
| `test-secure-data-loader` | Preloads sample secure data for testing. | To quickly test and understand the Secure RAG process. |
| `ollama` | Configures Ollama as the AI provider. | Local development with Ollama. |
| `openai` | Configures OpenAI as the AI provider. | Using OpenAI models (requires `OPENAI_API_KEY`). |
| `claude` | Configures Anthropic (Claude) as the AI provider. | Using Claude models (requires `ANTHROPIC_API_KEY`). |
| `elk-monitoring` | Enables logging and monitoring via ELK stack. | For deep log analysis and observability. |
| `grafana-monitoring` | Enables Micrometer metrics for Grafana. | For real-time performance dashboards. |

### Switching Profiles
You can switch profiles in your `application.yaml` or via command line:
```bash
./gradlew bootRun --args='--spring.profiles.active=history,ollama,grafana-monitoring'
```

This example builds an application with **history** enabled. In addition, it uses **Ollama** as the AI provider and **Grafana** for monitoring.

When using `openai` or `claude` profiles, ensure you have the corresponding API key set as an environment variable:
- For `openai`: `OPENAI_API_KEY`
- For `claude`: `ANTHROPIC_API_KEY`

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

---

## 5. Secure RAG (Metadata Filtering)

The `secure-rag` profile extends the standard RAG capabilities by adding a security layer that filters documents based on the logged-in user's permissions.

### Dependency on Auth Profiles
The `secure-rag` profile **must always be used with an authentication profile** (e.g., `auth-azure`). This is because the security filtering logic depends on the identity of the logged-in user. If you attempt to start the application with `secure-rag` but without an `auth-` profile, a startup error will be thrown by the `ProfileValidatorConfig` to prevent unsecured access.

### Preloading Test Data
To get a feel for how Secure RAG works, you can use the `test-secure-data-loader` profile. This will automatically preload two types of documents into your vector store:
- **PUBLIC**: Accessible by everyone.
- **CONFIDENTIAL**: Accessible only by users with the appropriate security level.

### Runtime Filtering vs. Startup Configuration
The filtering needs to be dynamic because the security level is determined by the **context of the logged-in user**. Since we want to apply different filters depending on who is asking the question, we must build the filter expression during the request execution. While static configuration is easier to manage, it doesn't allow for the user-specific context required for multi-level document security.

---

## 6. Database Migrations (Flyway)

In the `auth-azure` and `secure-rag` profiles, we use **Flyway** for database migrations.

### Why Flyway?
While Spring Boot's auto-DDL (`spring.jpa.hibernate.ddl-auto: update`) is convenient for quick prototyping, it is generally discouraged in **production-like environments**. 
- **Control**: Flyway gives you explicit control over how the schema evolves.
- **Versioning**: Every change is versioned (see `src/main/resources/db/migration`), making it easy to track what was applied and when.
- **Consistency**: It ensures that all environments (dev, test, prod) have exactly the same database schema, preventing "it works on my machine" issues caused by inconsistent table structures.

### Development Tips: Starting Fresh
As a developer, you might frequently switch between profiles or want to "start fresh" with your tables. Flyway tracks applied migrations in a table called `flyway_schema_history`.

If you want to re-run migrations from scratch:
1. Open your database tool (e.g., DBeaver or `psql`).
2. Delete the records from `flyway_schema_history` (except potentially the first one if you want to keep the baseline, or all of them if you are dropping the tables manually).
3. Restart the application.

This allows Flyway to see the migrations as "pending" again and re-execute them against your database.


