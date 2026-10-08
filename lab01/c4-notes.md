# C4 Architecture Notes (Lab 01)

## System Context Diagram (Level 1)
1. **Deliberately Omitted:** This diagram deliberately abstracts away branch network failover topologies, POS-side retry agents, underlying operating system environments, and internal database schemas.
2. **Quality-Attribute Scenarios:**
   - **QA-1 (Performance):** The system boundary is architected to digest 3 M rows and broadcast the report "ready" event within 60 seconds on an 8-core server.
   - **QA-3 (Availability):** The system boundary encapsulates partial-run fault tolerance, issuing partial reports within 90 seconds if a link (such as BTB's) drops before 06:00.
3. **Open Question for Client:** Can the operations team and finance director accept an automated re-run trigger upon late receipt of missing branch files, or must regenerated reports require manual operational sign-off?

---

## Container Diagram (Level 2)
1. **Deliberately Omitted:** This diagram deliberately omits in-memory calculation abstractions (such as pure-function calculation pipelines for QA-6), concrete thread-pool sizing for parallel streaming, and specific web session token caches.
2. **Quality-Attribute Scenarios:**
   - **Report Batch Engine [Java Streaming Engine & Template Renderers]** directly addresses **QA-1 (Performance)** by utilizing streaming and multi-threaded parallel aggregation on the 8-core host to keep processing times under 60 s for 3 M rows without exceeding 2 GB heap.
   - **Web Report Page [Web Application / Java HTTP Service]** directly addresses **QA-5 (Security)** by enforcing strict branch ID authorization tokens to block cross-branch access attempts (returning HTTP 403) and dispatching audit events to the Report Store in under 1 second.
3. **Open Question for Client:** Does the Landing Zone SFTP chroot need to support individual write-only credentials per branch POS terminal to prevent branches from inspecting or overwriting peer upload files?