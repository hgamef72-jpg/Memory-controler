# MemoryGuard

MemoryGuard is a Windows desktop system-monitoring and diagnostics application built with Kotlin and Compose Desktop.

## Current milestone — live telemetry foundation

The Overview dashboard now uses local operating-system data through OSHI/JNA instead of demo process rows.

Implemented:
- System-wide CPU load sampled from processor tick counters.
- Physical memory total, available, used bytes, and usage percentage.
- Live process enumeration with PID, name, state, resident memory, and CPU percentage.
- Disk read/write throughput from OS disk counters.
- Search across the live process list.
- Two-second telemetry refresh on a background dispatcher.
- Dark Compose Desktop dashboard with metric cards, process table, health panel, and activity panel.

The first sample is a baseline for interval-based CPU and disk rates; those rates become meaningful after the next sample. Process CPU is normalized by the logical processor count to match a system-capacity percentage.

## Architecture

- Kotlin + Compose Desktop for the desktop interface.
- OSHI/JNA for read-only hardware, operating-system, process, and disk statistics.
- Planned native C++20 Windows engine for supported advanced process and memory diagnostics.
- Planned JNA bridge for Kotlin/native interop.

## Planned modules

- Detailed process inspector and supported process-state operations.
- Thread inspection.
- Read-only virtual memory region inspection.
- Diagnostics and persistent activity/audit log.
- Additional Windows-specific system counters.

All operations remain subject to Windows access rights and process protections. MemoryGuard will not bypass protected-process controls, kernel protections, or access-control boundaries.

## Build

Requires JDK 17 and Gradle 8.10.2 or compatible.

Run the desktop application:

```powershell
gradle :app:run
```

Build an MSI on Windows:

```powershell
gradle :app:packageMsi
```

## CI installer

The Windows workflow compiles the application, builds the MSI, and uploads it as the `MemoryGuard-Windows-MSI` artifact. Open the Actions run for the latest commit, then download the artifact from the run's Artifacts section. Artifacts are retained for 14 days.

## Current limitations

- Process memory currently reports resident set size; it is not a private-memory measurement.
- CPU and disk rates are interval-based and require at least two samples.
- The advanced native engine, memory-map inspection, thread controls, signed installer, and release automation are not yet implemented.

Version 0.1.0 — development build.
