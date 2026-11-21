# MemoryGuard

MemoryGuard is a Windows system diagnostics and process-management desktop application.

## Current milestone

The repository contains the first Compose Desktop UI preview:

- Dark desktop layout with custom navigation.
- Overview dashboard with metric cards and sparkline charts.
- Searchable process table using clearly marked preview data.
- System health and recent activity panels.
- Windows MSI packaging configuration.

**Important:** dashboard values are mock preview data. The UI is not connected to live Windows telemetry yet.

## Architecture

- Kotlin + Compose Desktop for the desktop interface.
- Native C++20 Windows engine for supported process and memory diagnostics.
- JNA for Kotlin/native interop.
- Windows APIs with access checks and explicit audit logging.

## Planned modules

- Process enumeration and resource statistics.
- Thread inspection and supported process-state operations.
- Read-only virtual memory region inspection.
- Diagnostics and activity/audit log.

All operations remain subject to Windows access rights and process protections. MemoryGuard will not bypass protected-process controls, kernel protections, or access-control boundaries.

## Build

Requires JDK 17 and Gradle 8.10.2 or compatible.

```powershell
gradle :app:run
```

Build an MSI on Windows:

```powershell
gradle :app:packageMsi
```

## Status

Version 0.1.0 — UI foundation in progress. Native engine, live telemetry, automated tests, and signed installer are not implemented yet.
