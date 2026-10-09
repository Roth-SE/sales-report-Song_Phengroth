# Design Review

## Package Cohesion
*   **`model`**: Encapsulates the domain logic, invariants, and canonical data structures (records) representing sales metrics without knowing how they are stored or presented.
*   **`ingest`**: Handles the boundary layer responsible for reading external files, validating formats, parsing strings into domain objects, and emitting progression telemetry.
*   **`render`**: Formats and transforms computed domain metrics into external representation files (Text, HTML, CSV).

## Package Coupling & Instability (I = Ce / (Ca + Ce))
*   **`model`**: 
    *   Afferent (Ca): 2 (imported by `ingest` and `render`)
    *   Efferent (Ce): 0 (imports only `java.*`)
    *   Instability: I = 0 / (2 + 0) = **0.0 (Completely Stable)**
*   **`ingest`**: 
    *   Afferent (Ca): 1 (imported by `App` / entrypoint)
    *   Efferent (Ce): 1 (imports `model`)
    *   Instability: I = 1 / (1 + 1) = **0.5 (Balanced)**
*   **`render`**: 
    *   Afferent (Ca): 1 (imported by `App` / UI)
    *   Efferent (Ce): 1 (imports `model`)
    *   Instability: I = 1 / (1 + 1) = **0.5 (Balanced)**

## Accepted Coupling
It is entirely acceptable and by design that `ingest` and `render` couple directly to `model`. The `model` represents the core business logic (Clean Architecture inner ring). By keeping the `model` completely stable (I=0) and independent of I/O, the volatility of external formats (like new UI or ingest CSV rules) never impacts the core business calculations.

## Strategy Pattern: Sealed vs Open Interface for Modifiability (QA-4)
For QA-4 (Modifiability), an **open interface** is traditionally better than a sealed interface. An open interface adheres strictly to the Open/Closed Principle, allowing future developers to add PDF or XLSX renderers (as required by Chapter 08) simply by creating a new class, without touching existing source files. 

By contrast, Java's `sealed` interface explicitly forces modification of the parent interface's `permits` clause every time a new format is added, which slightly violates the goal of "modifying system behavior without touching unrelated modules." However, the sealed interface provides compile-time exhaustiveness checking (like in our factory `switch`), which increases system reliability by guaranteeing all known rendering variants are safely handled.