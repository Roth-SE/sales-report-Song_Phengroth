# ADR-0001: Adopt a modular monolith batch-processing architecture for Release 1

Status: Accepted
Date: 2026-10-09
Deciders: Song Phengroth

## Context
The Monthly Sales Report System must aggregate approximately 3 million transaction rows across 3 branches into a summarized monthly report within a tight 60-second window (QA-1). Modifiability is a key driver, requiring the team to adjust tax rules and report columns within 1 person-day (QA-4). The team has one semester to build, test, and safely extend the codebase. Network links to external branches (like BTB) are occasionally unreliable (QA-3), but the data processing itself happens centrally at the head office on an 8-core server. No solution is in place yet.

## Decision
We will build a modular monolith operating as a scheduled batch process running locally on the head-office 8-core server. The system will rely on strict package boundaries (model, ingest, render) to isolate concerns rather than network boundaries. Processing will be handled via in-memory parallel streaming rather than distributed computing frameworks.

## Alternatives considered
- **Event-driven microservices (e.g., Kafka/RabbitMQ):** Highly scalable and decoupled. We did not choose this because the operational overhead of managing message brokers and distributed transactions for a purely monthly batch process is too high. It also violates the requirement for a fast, zero-dependency local test suite (QA-6).
- **Distributed MapReduce (e.g., Spark/Hadoop):** Excellent for massive datasets. We did not choose this because 3 million rows (and the projected 9 million row growth, QA-2) easily fit into the 2 GB heap limit on a single 8-core machine. The JVM startup and cluster coordination overhead would jeopardize the 60-second execution target (QA-1).

## Consequences
+ Positive: Eliminating network hops during data aggregation allows us to easily hit the <= 60 s processing target by maximizing the 8-core CPU via parallel streams (QA-1).
+ Positive: A single deployable artifact with isolated internal modules keeps the development loop fast and modifiable without touching unrelated systems (QA-4).
- Negative: If the primary 8-core server hardware fails, the entire report engine goes offline, creating a single point of failure (QA-3).
- Negative: Scaling beyond the 2 GB heap target for 10+ branches (QA-2) will eventually require vertical scaling (buying more RAM/CPU) rather than simply adding horizontal nodes.

Revisit when: Monthly data volume consistently exceeds 10 million rows, or the business requires intra-day real-time dashboards rather than end-of-month reports.