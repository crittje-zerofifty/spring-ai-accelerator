# Feature development and decision making

- Use java 21 components where possible. For a List, for example, don't user .get(0), but getFirst()
- You are aware of Spring boot 4.x and Spring AI 2.0.0. You don't get confused because of old Spring Boot 3.x 
  documentation.
- Make small, targeted changes instead of building for hypothetical future needs.
- If something is unclear, ask before making assumptions.
- Adapters are classes that are used to adapt between different data sources. For instance an LLM adapter, rest 
  adapter, etc. Check if there is an existing adapter before creating a new one that is applicable to the use case. 
  Do not change an existing adapter unless there is a good reason to do so. Ask and explain why.
- When there is no duplicate class import, add the class to the imports at the top of the file. Do not prepend the 
  class name with the package. Only in case of duplicate classnames that have to be importated that is fine.

# Security and data handling

- Never log tokens or sensitive data.
- Sanitize all user input and use existing auth middleware.
- When creating a new table ensure there is a surrogate primary key called `id` (auto increment) and a natural key. Where 
  the natural key can be `articlenumber`. This is not a concatenated ID of the two keys. 

# Explicit prohibitions what agents must NOT do

- Do not bump major versions of core dependencies without a dedicated PR and discussion.
- Do not change database schema without a corresponding migration file.

# Architectural Decisions

## Core Principles
1. **Dependency Direction**: Infrastructure depends on Application. Application NEVER depends on Infrastructure.
2. **Ports**: All interactions with the outside world (DB, LLM, Web) must go through Interfaces defined in the `application.port` package.
  - `application.port.input`: Interfaces implemented by Usecases (Inbound).
  - `application.port.output`: Interfaces implemented by Infrastructure Adapters (Outbound).
3. **Usecases**: Business logic resides in `application.usecase`. Usecases must implement an input port.
4. **Spring Boot & Spring AI**:
  - Use **Spring AI** for LLM interactions (ChatClient, Advisors, Vector Stores).
  - Use **Dependency Injection** (Constructor-based) for wiring components.
  - Infrastructure components should be annotated with `@Component`, `@Service`, or `@RestController`.

## Layer Structure
- `nl.zerofifty.springaiaccelerator.application`:
  - `port.input`: Define what the system provides.
  - `port.output`: Define what the system needs.
  - `usecase`: Implementation of business logic.
- `nl.zerofifty.springaiaccelerator.infrastructure`:
  - `adapter`: Implementation of output ports (e.g., LLM clients, Repositories).
  - `controller`: Web entry points (Spring `@RestController`) calling input ports.
  - `config`: Spring `@Configuration` for wiring the application.

## Development Workflow
- When adding a new feature, start with the `application` layer:
  1. Define the Input/Output Ports.
  2. Create the Usecase implementing the Input Port.
  3. Implement the Adapter in `infrastructure`.
  4. Connect it via a Controller.
