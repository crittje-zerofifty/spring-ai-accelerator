# Spring AI Accelerator

[![CircleCI](https://dl.circleci.com/status-badge/img/gh/crittje-zerofifty/spring-ai-accelerator/tree/main.svg?style=svg&circle-token=CCIPRJ_q9Bocw8Sj2CV79YDpEWkZ_47d7f13ad0832ca4ec1dea898d50c271623f9a71)](https://dl.circleci.com/status-badge/redirect/gh/crittje-zerofifty/spring-ai-accelerator/tree/main)

## Table of Contents
- [Why this project?](#why-this-project)
- [1. Ollama Installation](#1-ollama-installation)
- [2. Architecture: Hexagonal & DDD](#2-architecture-hexagonal--ddd)
- [3. Modularity & Profiles](#3-modularity--profiles)
- [4. Monitoring Environments](#3-monitoring-environments)
- [5. MultiCloud OAuth2 Configuration](#4-multicloud-oauth2-configuration)
- [6. Secure RAG - Metadata Filtering - Query Expansion](#5-secure-rag-metadata-filtering---query-expansion)
- [7. AI Quality Assurance (LLM as-a-Judge)](#6-ai-quality-assurance-eval-testing)
- [8. AI Agent Configurations](#8-ai-agent-configurations)
- [9. Open Knowledge Framework (OKF)](#9-open-knowledge-framework-okf)
- [10. Database Migrations (Flyway)](#10-database-migrations-flyway)

In the rapidly evolving AI landscape, the vast majority of resources and boilerplates are tailored for Python 
(LangChain) or JavaScript. While great for prototyping, these often fall short when integrated into **Enterprise 
Java ecosystems**.

Java developers in corporate environments face unique challenges: strict security requirements, complex identity 
management, and the need for robust observability. **Spring AI Accelerator** was built to bridge this gap.

## Why this project?
1. **Production-Ready vs. Prototype**: Most AI examples stop at a simple prompt. This project starts with the "boring" 
but 
critical parts: **Azure Entra ID (OAuth2), Flyway migrations, Monitoring and Persistent JDBC Chat Memory**.

2. **The "Sovereign AI" Approach**: Companies should have full control over their data. By providing 
first-class support for **Ollama** and **PgVector**, this project enables high-performance RAG architectures that can 
run entirely on local or sovereign soil.

3. **Enterprise Observability**: Transitioning from a developer's laptop to production requires monitoring. It contains 
pre-integrated Grafana (Metrics, Traces, Logs) and ELK (via OTLP) so you can track token usage, latency, and model performance from day one.
All LLM Adapters (`LlmWithHistoryAdapter` and `LlmWithoutHistoryAdapter`) come with out-of-the-box monitoring via OpenTelemetry.

4. **Architectural Flexibility**: Using a profile-based, provider-agnostic design, you can switch between Claude, 
OpenAI, 
or local Ollama models without changing a single line of business logic. This prevents vendor lock-in and future-proofs your AI strategy.

### Developer-First Experience
Done: The heavy lifting of navigating the early "milestone" versions of Spring AI, so you don't have to. But we went 
further than just providing a working demo:

- **Hexagonal Architecture (Ports & Adapters)**: The project is structured to keep the AI logic decoupled from external 
providers. This ensures that switching from OpenAI to a local Ollama instance—or swapping your database—doesn't leak infrastructure details into your core domain.

- **Domain-Driven Design (DDD)**: Organized the codebase around clear domain boundaries. This makes the project 
highly scalable and ensures that as your AI features grow, your code remains organized and meaningful.
## 1. Ollama Installation

To run Large Language Models locally, we recommend using **Ollama**.

### Why Not Docker?
While you can run Ollama via Docker, we generally recommend a **native installation** for most users. Running an LLM inside a Docker container adds an extra virtualization layer that can impact performance, especially when it comes to GPU acceleration.

#### Linux & Docker Performance
On **Linux**, you can achieve near-native performance by using the NVIDIA Container Toolkit and passing the GPU through with the `--gpus all` flag. 

- **Pros**: 
    - Isolated environment.
    - Performance is almost identical to native (as Docker on Linux shares the host kernel).
- **Cons**: 
    - Complex setup (requires NVIDIA Container Toolkit).
    - Driver version mismatches can cause troubleshooting headaches.
    - Potential overhead in memory management compared to bare metal.

**Is it "good enough"?**
Yes. For many production environments, the trade-off for containerization is worth the minimal performance hit. Benchmarks show that when properly configured, the overhead is negligible (typically < 1-2%).

**References**:
- [Ollama Docker Hub - GPU Acceleration](https://hub.docker.com/r/ollama/ollama)
- [NVIDIA Container Toolkit Documentation](https://docs.nvidia.com/datacenter/cloud-native/container-toolkit/latest/install-guide.html)
- [Benchmarking GPU Passthrough Performance on Docker for AI Cloud System](https://jurnal.itscience.org/index.php/brilliance/article/view/6794#:~:text=In%20another%20case%2C%20GPU%20passthrough,deployment%20in%20a%20limited%20environment.) (Example source for performance parity)
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
| `auth-aws` | Enables AWS Cognito OAuth2 authentication. | Production or secured environments requiring user login. |
| `rag` | Enables Retrieval Augmented Generation. | When you want the LLM to use your own data/documents. |
| `secure-rag` | Enables Secure RAG with metadata filtering. | When you need to restrict document access based on user security levels. |
| `test-secure-data-loader` | Preloads sample secure data for testing. | To quickly test and understand the Secure RAG process. |
| `ollama` | Configures Ollama as the AI provider. | Local development with Ollama. |
| `openai` | Configures OpenAI as the AI provider. | Using OpenAI models (requires `OPENAI_API_KEY`). |
| `claude` | Configures Anthropic (Claude) as the AI provider. | Using Claude models (requires `ANTHROPIC_API_KEY`). |
| `eval-testing` | Enables AI Quality Assurance (LLM-as-a-Judge). | To automatically evaluate RAG responses for faithfulness and relevance. Results are published via Micrometer Observations. |
| `okf` | Enables Open Knowledge Framework (OKF) data loading and processing. | When you want to use structured OKF knowledge for domain-specific tasks like expense auditing. Requires `rag` profile. |

### Switching Profiles
You can switch profiles in your `application.yaml` or via command line:
```bash
./gradlew bootRun --args='--spring.profiles.active=history,ollama,eval-testing'
```

This example builds an application with **history** enabled. In addition, it uses **Ollama** as the AI provider.

When using `openai` or `claude` profiles, ensure you have the corresponding API key set as an environment variable:
- For `openai`: `OPENAI_API_KEY`
- For `claude`: `ANTHROPIC_API_KEY`

### API Documentation (Swagger)
When the application is running, you can access the interactive API documentation (Swagger UI) at:
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 4. Monitoring Environments

Setup your monitoring stack locally using the provided Docker Compose files in the `/monitoring` directory.

### OpenTelemetry & Micrometer Observation API

The project uses the **Micrometer Observation API** for vendor-neutral instrumentation. This means your application code doesn't know about specific monitoring tools like Grafana or ELK. Data is exported via the **OpenTelemetry (OTLP) protocol**.

The `LlmWithHistoryAdapter` and `LlmWithoutHistoryAdapter` are instrumented to provide out-of-the-box monitoring of LLM interactions, including token usage and latency, which are then exported via OpenTelemetry.

- **Grafana Stack (Prometheus, Tempo, Loki)**: 
  - **Focus**: Unified Observability (Metrics, Traces, Logs). 
  - **Usage**: Best for real-time dashboards and distributed tracing. Traces are automatically correlated with logs.
- **ELK Stack (Elasticsearch, Logstash, Kibana)**: 
  - **Focus**: Log Analysis and Search. 
  - **Usage**: Use this when you need deep search capabilities across your logs. ELK can also receive OTLP data.

### Unified Monitoring (Grafana)
1. Start the services:
   ```bash
   docker-compose -f monitoring/docker-compose-accelerator-grafana-monitoring.yml up -d
   ```
2. The application automatically exports data via OTLP to `http://localhost:4318`.
3. **Access Dashboards**:
   - **Grafana**: [http://localhost:3000](http://localhost:3000) (Default login: `admin` / `admin`).

### ELK Stack
1. Start the services:
   ```bash
   docker-compose -f monitoring/docker-compose-accelerator-elk-monitoring.yml up -d
   ```
2. **Access Kibana**: Open [http://localhost:5601](http://localhost:5601) in your browser.

### Configuration for Hosted (Non-Local) Environments

If you are moving away from `localhost` to a hosted monitoring stack (e.g., Elastic Cloud, Managed Grafana, or a central company server), you need to update the connection points:

#### 1. Hosted Monitoring
- **OTLP Destination**: Update `src/main/resources/application.yaml`. Change the OTLP endpoints to your hosted collector's URL.
- **Authentication**: If your hosted collector requires authentication (e.g., via headers), you may need to add additional OTEL configuration.

---

## 4. MultiCloud OAuth2 Configuration

### Azure

To use Azure AD for authentication, you need to register an application in the Azure Portal and configure the following.

#### Azure Setup Requirements
1. **App Registration**: Create a new registration in "App registrations".
2. **Redirect URI**: Set it to `http://localhost:8080/login/oauth2/code/azure`.
3. **Client Secret**: Generate a new client secret.
4. **API Permissions**: Ensure `openid`, `profile`, and `email` are granted.

#### Configuration
Use the `auth-azure` profile and provide the following environment variables:

- `AZURE_CLIENT_ID`: Your Azure Application (client) ID.
- `AZURE_CLIENT_SECRET`: Your Azure Client Secret.

The issuer URI is pre-configured in `application-auth-azure.yaml`, but you may need to update the Tenant ID in the URL:
`https://login.microsoftonline.com/{your-tenant-id}/v2.0`

### AWS (Cognito)

To use AWS Cognito for authentication, you need to configure a User Pool and an App Client.

#### AWS Setup Requirements
1.  **App Client**: Create a new App Client in your User Pool.
2.  **Redirect URI**: Set it to `http://localhost:8080/login/oauth2/code/cognito`.
3.  **Allowed OAuth Flows**: Must include **Authorization code grant**.
4.  **Allowed OAuth Scopes**: Ensure `openid`, `profile`, and `email` are selected.
5.  **Attribute Read Permissions**: Ensure the App Client has read access to the `email` attribute.

#### Configuration
Use the `auth-aws` profile and provide the following environment variables (or update `application-auth-aws.yml`):

- `COGNITO_CLIENT_ID`: Your Cognito App Client ID.
- `COGNITO_CLIENT_SECRET`: Your Cognito App Client Secret.

The `issuer-uri` in `application-auth-aws.yml` should point to your Cognito User Pool:
`https://cognito-idp.{region}.amazonaws.com/{user-pool-id}`

#### Why OAuth2 over Native AWS IAM/ARN?
While AWS offers powerful native identity features like **IAM roles** and **ARNs**, this project explicitly uses **OAuth2/OIDC**. This choice ensures the Spring AI Accelerator remains **cloud-agnostic and consistent**. By using a standard protocol, the implementation details of "who the user is" remain the same whether you are on Azure, AWS, or an on-premise Keycloak instance.

#### Importance of Scopes and Attributes
During the setup of this project, we encountered specific requirements that highlight the importance of correct configuration:
- **User Name Attribute**: We configure `user-name-attribute: email`. This is critical for our **Secure RAG** implementation because the document permissions in our example database are mapped to **email addresses**. By using email as the primary identifier, we can seamlessly filter vector search results based on the logged-in user's identity.
- **Grant Types**: Ensure "Authorization code grant" is used. "Client Credentials" is for machine-to-machine and won't provide the user-specific scopes (like email) needed for personalized RAG.

#### How it works in practice

##### For Web Users
When a user accesses the service, they will be redirected to the configured Identity Provider (Microsoft or Cognito) login page. After successful authentication, a session is created. The user's email is extracted from the OIDC token and used to sandbox their chat history.

#### For REST/API Calls
Currently, the service is configured for **OAuth2 Login** (Authorization Code Flow). 
- **Browser-based**: Works automatically via session cookies after login.
- **Service-to-Service**: If you want to call this as a pure REST API with a `Bearer` token from a mobile app or another service, you would need to adjust the `AzureSecurityConfig` to support `oauth2ResourceServer`. 

When `auth-azure` is active, the `/chat` endpoint expects the user to be authenticated. If you call it without a valid session, you will receive a `401 Unauthorized` or be redirected to the login page.

---

## 5. Secure RAG - Metadata Filtering - Query Expansion

The `secure-rag` profile extends the standard RAG capabilities by adding a security layer that filters documents based on the logged-in user's permissions.

### Dependency on Auth Profiles
The `secure-rag` profile **must always be used with an authentication profile** (e.g., `auth-azure` or `auth-aws`). This is because the security filtering logic depends on the identity of the logged-in user. If you attempt to start the application with `secure-rag` but without an `auth-` profile, a startup error will be thrown by the `ProfileValidatorConfig` to prevent unsecured access.

### Preloading Test Data
To get a feel for how Secure RAG works, you can use the `test-secure-data-loader` profile. This will automatically preload two types of documents into your vector store:
- **PUBLIC**: Accessible by everyone.
- **CONFIDENTIAL**: Accessible only by users with the appropriate security level.

### Runtime Filtering vs. Startup Configuration
The filtering needs to be dynamic because the security level is determined by the **context of the logged-in user**. Since we want to apply different filters depending on who is asking the question, we must build the filter expression during the request execution. While static configuration is easier to manage, it doesn't allow for the user-specific context required for multi-level document security.

### Improving Retrieval: Multi-Querying (Query Expansion)
A common challenge in RAG is retrieving all relevant documents when a user query covers multiple topics. For example, a query like:
`What can you tell me about codebase x and i also need some parking policy`

might only return documents for one of the topics if the embedding is skewed. To overcome this, you can use the `query-expansion` profile.

The `query-expansion` profile enables the `QueryExpansionAdvisor`. This advisor:
1.  Takes the original user question.
2.  Uses an LLM to generate multiple alternative versions of the question (Multi-Querying).
3.  Performs a similarity search for *each* generated question.
4.  Combines and deduplicates the retrieved documents.
5.  Passes the enriched context to the final prompt.
6.  **Tip**: Use a cheaper/faster model (like GPT-3.5 or Claude Haiku) for query expansion to keep latency and costs low, while using a more powerful model for the final response.

To use it, activate the `query-expansion` profile along with a RAG profile:
`--spring.profiles.active=rag,query-expansion,openai`

---

## 6. AI Quality Assurance (LLM as-a-Judge)

The `eval-testing` profile implements a **LLM-as-a-Judge** pattern to provide automated Quality Assurance for your AI responses.

### How it Works
When active, the `EvaluateQualityAdvisor` intercepts the LLM response stream. Once the stream is complete, it triggers an asynchronous evaluation process that rates the response on:
- **Faithfulness**: Is the answer derived ONLY from the provided context?
- **Relevancy**: Does the answer actually address the user's question?

### Observability Integration
The evaluation results are not just logged; they are published to your monitoring stack:
- **Grafana**: Track faithfulness and relevancy scores over time to detect model drift or retrieval issues.
- **ELK Stack**: Store detailed evaluation reasoning alongside the original prompts and answers for deep-dive forensic analysis.

This "side-car" evaluation ensures that you maintain high response quality without blocking the user-friendly streaming experience.

---

## 8. AI Agent Configurations

To ensure consistency and adherence to our architectural standards when using AI coding assistants, this project 
includes dedicated rule files for various AI agents. These files define the project's core principles, layer structure, and development workflow.

- **`.clinerules`**: Configuration for [Cline](https://cline.bot/).
- **`.cursorrules`**: Configuration for [Cursor](https://cursor.com/). 
- **`claude.md`**: Configuration for [Claude](https://claude.ai/) or other LLMs.

By maintaining these files, we ensure that any AI assistant—regardless of the platform—operates with a shared understanding of the **Spring AI Accelerator's** high-quality standards.

---

## 9. Open Knowledge Framework (OKF)

The `okf` profile introduces support for Google's **Open Knowledge Framework**, allowing you to load domain-specific knowledge in a structured YAML format.

### Why OKF?
OKF provides a standardized way to define domains, entities, and compliance rules. In this project, we use it to represent corporate policies and AI agent behaviors, which are then converted into Spring AI `Document` instances with enriched metadata for precise RAG filtering.

#### OKF vs. Traditional RAG

While standard RAG is powerful for searching through large volumes of unstructured text, OKF offers several advantages and tradeoffs:

**Pros (Benefits over RAG):**
- **Reduced Hallucinations**: Traditional RAG relies on splitting documents into arbitrary chunks. This can break context (e.g., a rule and its exception being in different chunks). OKF uses structured YAML where related entities and rules are kept together, significantly reducing hallucinations caused by context fragmentation.
- **Precise Metadata Filtering**: Because OKF is structured, every piece of information comes with rich, typed metadata. This allows for highly precise vector search filtering (e.g., "only look at compliance rules for the 'finance' domain") which is more reliable than purely semantic search.
- **Improved Context Injection**: Instead of injecting raw text snippets, OKF allows for injecting structured knowledge that the LLM can parse more effectively, leading to more deterministic and auditable responses.

**Cons (Tradeoffs):**
- **Higher Curation Effort**: Unlike RAG, where you can just dump PDF/Text files into a vector store, OKF requires manual effort or specialized tools to structure knowledge into YAML format.
- **Schema Rigidity**: OKF follows a specific schema. If your domain data doesn't fit well into the `entity/domain/rule` structure, it might feel restrictive compared to the "anything goes" approach of unstructured RAG.
- **Maintenance Overhead**: Keeping YAML files in sync with evolving corporate policies requires a more disciplined documentation process.

### Running with OKF
The `okf` profile requires the `rag` profile to be active. A common "sovereign" setup using local models would be:
```bash
./gradlew bootRun --args='--spring.profiles.active=ollama,rag,okf'
```

### Feature: Employee Expense Auditing
When the `okf` profile is active, a specialized `EmployeeExpenseController` is enabled at `/api/v1/expense/process`. This endpoint uses the loaded OKF policy data to evaluate expense claims against company rules using the LLM.

The application returns a structured `ExpenseAuditResponse` DTO, ensuring high reliability for business logic and UI rendering.

The following http request with body:
```json
{
  "recipe": "total amount: 100 euro, 5 euro for sandwitch, 8 euro for beers, 12 euro for souvinier of client, 75 euro a new Ferrari"
}
```

Possibly results in a response like:
```json
{
  "items": [
    {
      "index": 0,
      "subject": "Food",
      "amount": 5.0,
      "status": "APPROVED",
      "reason": "Expense for sandwich is within the meal cap."
    },
    {
      "index": 1,
      "subject": "Drinks",
      "amount": 8.0,
      "status": "REJECTED",
      "reason": "Alcohol is not allowed."
    },
    {
      "index": 2,
      "subject": "Gifts",
      "amount": 12.0,
      "status": "ESCALATED",
      "reason": "Souvenir is a luxury service and exceeds the limit of non-business related expenses."
    },
    {
      "index": 3,
      "subject": "Miscellaneous",
      "amount": 75.0,
      "status": "ESCALATED",
      "reason": "An expense on a Ferrari is suspicious and exceeds the limit of non-business related expenses."
    }
  ],
  "summary": "Total approved amount: 5 EUR. Total escalated amount: 87 EUR.",
  "totalApprovedAmount": 5.0
}

```

And given the escalation rules it send out an email (log in the example) to the designated manager as per the OKF. 
See the below details.


### Feature: Knowledge-Driven Escalation
The `okf` profile also demonstrates an **Agentic Workflow** for handling policy violations. If an expense is flagged as `ESCALATED` by the LLM (e.g., high-value items like a Jacuzzi), the system automatically triggers a reactive side-effect:
1.  **Contextual Resolution**: It parses the OKF metadata from the retrieved documents to find the `escalates_to` relationship (e.g., `role_finance_manager`).
2.  **Virtual Escalation**: It calls an `EscalationOutputPort` which, in the demonstration adapter, logs a "VIRTUAL EMAIL" to the console with all relevant details and the resolved recipient.

This showcases how OKF structured knowledge can be used not just for prompt context, but to drive application logic and routing decisions.

---

## 10. Database Migrations (Flyway)

In the `auth-azure`, `auth-aws` and `secure-rag` profiles, we use **Flyway** for database migrations.

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


