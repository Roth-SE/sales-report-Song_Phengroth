# ATAM-style evaluation: release 1 architecture

## Business drivers
- **Timeliness:** Report for month M on the morning of day 2 of M+1, ideally faster as the company expands.
- **Accuracy & Security:** Strict branch data isolation; the finance director needs an accurate audit trail, and branch managers must only see their own numbers.
- **Resilience:** Unreliable branch networks (BTB link) must not break the whole daily pipeline.
- **Maintainability:** Tax rules and reporting columns change frequently and must be easy to adapt.

## Candidates

### A: Modular monolith batch
```mermaid
flowchart LR
  files[(Landing zone)] --> batch[Report batch: ingest, engine, render]
  batch --> store[(Report store)]