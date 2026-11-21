package memoryguard.monitor

import oshi.SystemInfo
import oshi.software.os.OSProcess
import java.util.Comparator

/**
 * Read-only system telemetry backed by OSHI/JNA.
 *
 * This class only queries operating-system statistics. It does not open,
 * suspend, terminate, or modify other processes.
 */
class SystemMonitor {
    private val systemInfo = SystemInfo()
    private val processor = systemInfo.hardware.processor
    private val memory = systemInfo.hardware.memory
    private val operatingSystem = systemInfo.operatingSystem

    private var previousCpuTicks: LongArray = processor.systemCpuLoadTicks

    data class ProcessSnapshot(
        val pid: Int,
        val name: String,
        val memoryBytes: Long,
        val state: String
    )

    data class Snapshot(
        val cpuLoadPercent: Double,
        val memoryTotalBytes: Long,
        val memoryAvailableBytes: Long,
        val processCount: Int,
        val logicalProcessorCount: Int,
        val processes: List<ProcessSnapshot>
    ) {
        val memoryUsedBytes: Long
            get() = (memoryTotalBytes - memoryAvailableBytes).coerceAtLeast(0L)

        val memoryUsedPercent: Double
            get() = if (memoryTotalBytes > 0L) {
                memoryUsedBytes.toDouble() / memoryTotalBytes.toDouble() * 100.0
            } else {
                0.0
            }
    }

    /**
     * Takes a non-blocking sample. CPU usage is calculated between this
     * invocation and the previous one, so the first sample may be near zero.
     */
    @Synchronized
    fun sample(processLimit: Int = 100): Snapshot {
        val currentTicks = processor.systemCpuLoadTicks
        val cpuLoad = processor.getSystemCpuLoadBetweenTicks(previousCpuTicks, currentTicks)
            .coerceIn(0.0, 1.0)
        previousCpuTicks = currentTicks

        val processes = operatingSystem.getProcesses(
            null,
            Comparator.comparingLong<OSProcess> { it.residentSetSize }.reversed(),
            processLimit.coerceIn(1, 500)
        ).map { process ->
            ProcessSnapshot(
                pid = process.processID,
                name = process.name,
                memoryBytes = process.residentSetSize.coerceAtLeast(0L),
                state = process.state.name
            )
        }

        return Snapshot(
            cpuLoadPercent = cpuLoad * 100.0,
            memoryTotalBytes = memory.total,
            memoryAvailableBytes = memory.available,
            processCount = operatingSystem.processCount,
            logicalProcessorCount = processor.logicalProcessorCount,
            processes = processes
        )
    }
}
