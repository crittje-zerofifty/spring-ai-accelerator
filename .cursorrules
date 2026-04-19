# Project Architectural Rules: Hexagonal & DDD

This project follows Hexagonal Architecture (Ports and Adapters) and Domain-Driven Design (DDD) principles.

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
