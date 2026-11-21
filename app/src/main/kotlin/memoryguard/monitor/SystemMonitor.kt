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
    private var previousProcesses: Map<Int, OSProcess> = emptyMap()

    data class ProcessSnapshot(
        val pid: Int,
        val name: String,
        val memoryBytes: Long,
        val cpuPercent: Double,
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
        val cpuLoad = processor.getSystemCpuLoadBetweenTicks(previousCpuTicks)
            .coerceIn(0.0, 1.0)
        previousCpuTicks = currentTicks

        val currentProcesses = operatingSystem.getProcesses(
            null,
            Comparator.comparingLong<OSProcess> { it.residentSetSize }.reversed(),
            processLimit.coerceIn(1, 500)
        )
        val processes = currentProcesses.map { process ->
            val previous = previousProcesses[process.processID]
            val cpuPercent = previous?.let { prior ->
                runCatching { process.getProcessCpuLoadBetweenTicks(prior) * 100.0 }
                    .getOrDefault(0.0)
                    .coerceIn(0.0, 100.0)
            } ?: 0.0
            ProcessSnapshot(
                pid = process.processID,
                name = process.name,
                memoryBytes = process.residentSetSize.coerceAtLeast(0L),
                cpuPercent = cpuPercent,
                state = process.state.name
            )
        }
        previousProcesses = currentProcesses.associateBy { it.processID }

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
