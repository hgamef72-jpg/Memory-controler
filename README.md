# MemoryGuard

MemoryGuard is a Windows process and memory diagnostics application.

## Planned architecture

- Kotlin + Compose Desktop for the user interface.
- A native C++20 Windows engine for supported process diagnostics and operations.
- JNA for the Kotlin/native boundary.

## Initial scope

- Enumerate processes and display resource usage.
- Inspect threads and supported process state operations.
- Enumerate virtual memory regions in read-only mode.
- Record user-initiated actions in an audit log.

Operations are limited by Windows access rights and process protection. The application will not bypass protected-process controls, kernel protections, or access-control boundaries.

## Development status

Repository initialized. Implementation and automated validation are pending.
