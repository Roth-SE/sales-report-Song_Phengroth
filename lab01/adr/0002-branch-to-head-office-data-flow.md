# ADR-0002: Branches push daily CSVs via SFTP with partial-run and idempotent retry policies

Status: Accepted
Date: 2026-10-09
Deciders: Song Phengroth

## Context
Data originates at branch POS systems (PNH, REP, BTB) and must securely reach the head office for the 06:00 report execution (QA-1). The network link to BTB is known to be unreliable and often drops (QA-3). The finance director requires accurate audit trails and strict isolation so branches cannot view or overwrite each other's data (QA-5). We must decide how data travels and how the system handles the inevitable late arrivals without halting the entire reporting process.

## Decision
We will require branch POS systems to push one CSV file per day to a secure head-office SFTP Landing Zone. 
**Failure Policy (QA-3):** If a file is missing by the scheduled 06:00 run, the system will not wait. It will immediately generate a partial report using the available branch data and explicitly flag the missing branch as "data missing". A background retry daemon will check the Landing Zone every 5 minutes. Once the late BTB data arrives, the full report is regenerated automatically.
**Idempotency & Merging:** Files will be strictly named `BRANCH-yyyy-MM-dd.csv`. Upon reading, the ingest job calculates a SHA-256 checksum. If a file is re-uploaded, the system replaces all database records matching that `(Branch, Date)` composite key before regenerating the report, guaranteeing no duplicated rows.

## Alternatives considered
- **Head office pulls from each branch:** Head office cron jobs connect to branch networks to download files. We did not choose this because it requires opening inbound firewall ports at every branch store, creating security vulnerabilities (QA-5), and causes the head office to hang or fail if a branch network goes completely offline.
- **POS publishes one event per receipt (Message Broker):** Each POS sends individual transactions directly to a central cloud queue in real-time. We did not choose this because it requires rewriting the legacy POS software and introduces complex local-caching logic at the branch to handle message loss when the BTB link drops.

## Consequences
+ Positive: Generates the partial report on time (<= 90 s after 06:00) satisfying the finance director's deadline even when BTB is offline (QA-3).
+ Positive: SFTP chroot jails natively enforce security isolation, guaranteeing branches cannot read or alter another branch's uploads (QA-5).
- Negative: Idempotent upserts increase the complexity of the database layer, as the ingest job must execute atomic `DELETE` + `INSERT` transactions for a given date rather than blindly appending rows.
- Negative: The operations person will receive multiple notification emails (partial failure, retry success) which could cause alert fatigue (QA-3).

Revisit when: Branch network infrastructure is upgraded to 99.99% uptime, removing the need for aggressive partial-run strategies.