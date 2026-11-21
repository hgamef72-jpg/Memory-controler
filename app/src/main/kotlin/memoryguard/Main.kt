package memoryguard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Locale
import memoryguard.monitor.SystemMonitor

private val Bg = Color(0xFF0B0E14)
private val Sidebar = Color(0xFF0E131C)
private val Panel = Color(0xFF131A25)
private val PanelAlt = Color(0xFF171F2C)
private val Stroke = Color(0xFF263244)
private val Muted = Color(0xFF8491A5)
private val TextMain = Color(0xFFE8EDF5)
private val Blue = Color(0xFF5B8CFF)
private val Green = Color(0xFF35C995)
private val Amber = Color(0xFFFFB454)
private val Red = Color(0xFFFF657A)

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "MemoryGuard",
        state = rememberWindowState(width = 1440.dp, height = 900.dp)
    ) {
        MemoryGuardApp()
    }
}

@Composable
private fun MemoryGuardApp() {
    var selectedPage by remember { mutableStateOf("Overview") }
    var query by remember { mutableStateOf("") }
    val monitor = remember { SystemMonitor() }
    var telemetry by remember { mutableStateOf<SystemMonitor.Snapshot?>(null) }

    LaunchedEffect(monitor) {
        while (true) {
            val sample = runCatching {
                withContext(Dispatchers.IO) { monitor.sample(100) }
            }.getOrNull()
            if (sample != null) telemetry = sample
            delay(1_000)
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Bg,
        surface = Panel,
        primary = Blue,
        onBackground = TextMain,
        onSurface = TextMain
    )) {
        Row(Modifier.fillMaxSize().background(Bg)) {
            SidebarNavigation(selectedPage = selectedPage, onSelect = { selectedPage = it })
            Column(
                Modifier.weight(1f).fillMaxHeight().padding(horizontal = 30.dp, vertical = 24.dp)
            ) {
                TopBar(selectedPage)
                Spacer(Modifier.height(24.dp))
                if (selectedPage == "Overview") {
                    OverviewPage(query = query, onQueryChange = { query = it }, telemetry = telemetry)
                } else {
                    PlaceholderPage(selectedPage)
                }
            }
        }
    }
}

@Composable
private fun SidebarNavigation(selectedPage: String, onSelect: (String) -> Unit) {
    Column(
        Modifier.width(236.dp).fillMaxHeight().background(Sidebar)
            .border(width = 1.dp, color = Stroke.copy(alpha = 0.55f))
            .padding(horizontal = 18.dp, vertical = 22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Blue),
                contentAlignment = Alignment.Center
            ) {
                Text("M", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(11.dp))
            Column {
                Text("MEMORYGUARD", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                Text("SYSTEM CONTROL", color = Muted, fontSize = 9.sp, letterSpacing = 1.5.sp)
            }
        }
        Spacer(Modifier.height(38.dp))
        Text("WORKSPACE", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp)
        Spacer(Modifier.height(12.dp))
        listOf("Overview", "Processes", "Memory map", "Threads", "Diagnostics").forEachIndexed { index, item ->
            NavigationItem(icon = listOf("◫", "▤", "▦", "⑂", "⌁")[index], label = item,
                selected = selectedPage == item, onClick = { onSelect(item) })
        }
        Spacer(Modifier.height(26.dp))
        Text("MANAGEMENT", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp)
        Spacer(Modifier.height(12.dp))
        NavigationItem("◷", "Activity log", selectedPage == "Activity log") { onSelect("Activity log") }
        NavigationItem("⚙", "Settings", selectedPage == "Settings") { onSelect("Settings") }
        Spacer(Modifier.weight(1f))
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Panel).padding(14.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(Amber))
                    Spacer(Modifier.width(7.dp))
                    Text("DEVELOPMENT BUILD", color = Amber, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                }
                Spacer(Modifier.height(8.dp))
                Text("Native engine is not connected yet.", color = Muted, fontSize = 11.sp, lineHeight = 16.sp)
                Spacer(Modifier.height(8.dp))
                Text("VERSION 0.1.0 · LIVE TELEMETRY", color = Muted.copy(alpha = 0.7f), fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun NavigationItem(icon: String, label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) Color(0xFF1B2A43) else Color.Transparent
    val fg = if (selected) TextMain else Muted
    Surface(
        onClick = onClick,
        color = bg,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, color = if (selected) Blue else Muted, fontSize = 17.sp)
            Spacer(Modifier.width(12.dp))
            Text(label, color = fg, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
        }
    }
}

@Composable
private fun TopBar(page: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(page, color = TextMain, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Monitor, inspect and manage your Windows system", color = Muted, fontSize = 12.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.clip(CircleShape).background(Color(0xFF192B27)).padding(horizontal = 10.dp, vertical = 7.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(Green))
                    Spacer(Modifier.width(7.dp))
                    Text("LOCAL SESSION", color = Green, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                }
            }
            Spacer(Modifier.width(12.dp))
            Box(Modifier.clip(RoundedCornerShape(10.dp)).background(Panel).border(1.dp, Stroke, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 9.dp)) {
                Text("⌘  Ctrl + K", color = Muted, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun OverviewPage(query: String, onQueryChange: (String) -> Unit, telemetry: SystemMonitor.Snapshot?) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            MetricCard("CPU USAGE", telemetry?.let { oneDecimal(it.cpuLoadPercent) } ?: "—", "%", telemetry?.let { "${it.logicalProcessorCount} logical cores" } ?: "Waiting for system sample", Blue, listOf(20f, 26f, 18f, 35f, 29f, 42f, 31f, 25f, 38f, 18f))
            MetricCard("MEMORY", telemetry?.let { oneDecimal(it.memoryUsedBytes / 1_073_741_824.0) } ?: "—", "GB", telemetry?.let { "of ${oneDecimal(it.memoryTotalBytes / 1_073_741_824.0)} GB · ${oneDecimal(it.memoryUsedPercent)}%" } ?: "Waiting for system sample", Color(0xFF9A7BFF), listOf(30f, 32f, 31f, 35f, 34f, 36f, 38f, 37f, 39f, 39f))
            MetricCard("DISK ACTIVITY", telemetry?.let { oneDecimal((it.diskReadBytesPerSecond + it.diskWriteBytesPerSecond) / 1_048_576.0) } ?: "—", "MB/s", telemetry?.let { "R: ${oneDecimal(it.diskReadBytesPerSecond / 1_048_576.0)} · W: ${oneDecimal(it.diskWriteBytesPerSecond / 1_048_576.0)}" } ?: "Waiting for disk sample", Amber, listOf(12f, 18f, 14f, 28f, 20f, 15f, 32f, 22f, 26f, 18f))
            MetricCard("PROCESSES", telemetry?.processCount?.toString() ?: "—", "", "Detected by OSHI", Green, listOf(26f, 25f, 26f, 28f, 26f, 27f, 25f, 29f, 27f, 28f))
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1.8f).fillMaxHeight()) {
                ProcessPanel(query, onQueryChange, telemetry?.processes?.map { p -> ProcessInfo(p.name, p.pid.toString(), "${oneDecimal(p.cpuPercent)}%", formatBytes(p.memoryBytes), p.state, if (p.state == "RUNNING") Green else Muted) } ?: emptyList(), Modifier.weight(1f))
            }
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SystemHealthPanel(telemetry, Modifier.weight(1.05f))
                ActivityPanel(Modifier.weight(0.95f))
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(if (telemetry == null) "WAITING FOR LOCAL SYSTEM TELEMETRY" else "LIVE LOCAL TELEMETRY · REFRESH 1s · READ-ONLY", color = Muted.copy(alpha = 0.65f), fontSize = 9.sp, letterSpacing = 0.8.sp)
    }
}

@Composable
private fun RowScope.MetricCard(title: String, value: String, unit: String, subtitle: String, accent: Color, points: List<Float>) {
    Column(
        Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(Panel)
            .border(1.dp, Stroke.copy(alpha = 0.8f), RoundedCornerShape(16.dp)).padding(17.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = Muted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
            Box(Modifier.size(7.dp).clip(CircleShape).background(accent))
        }
        Spacer(Modifier.height(13.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, color = TextMain, fontSize = 27.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(5.dp))
            Text(unit, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
        }
        Text(subtitle, color = Muted, fontSize = 10.sp)
        Spacer(Modifier.height(10.dp))
        Sparkline(points, accent, Modifier.fillMaxWidth().height(30.dp))
    }
}

@Composable
private fun Sparkline(points: List<Float>, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        if (points.size < 2) return@Canvas
        val max = (points.maxOrNull() ?: 1f).coerceAtLeast(1f)
        val min = points.minOrNull() ?: 0f
        val range = (max - min).coerceAtLeast(1f)
        val path = Path()
        points.forEachIndexed { index, value ->
            val x = size.width * index / (points.size - 1)
            val y = size.height - ((value - min) / range * (size.height * 0.78f)) - size.height * 0.1f
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
        drawLine(color.copy(alpha = 0.12f), Offset(0f, size.height - 1f), Offset(size.width, size.height - 1f), 1.dp.toPx())
    }
}

private data class ProcessInfo(val name: String, val pid: String, val cpu: String, val memory: String, val status: String, val tint: Color)

@Composable
private fun ProcessPanel(query: String, onQueryChange: (String) -> Unit, processes: List<ProcessInfo>, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(Panel).border(1.dp, Stroke, RoundedCornerShape(16.dp))) {
        Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Top processes", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text("Most active applications by resource usage", color = Muted, fontSize = 10.sp)
            }
            OutlinedButton(onClick = {}, shape = RoundedCornerShape(9.dp), contentPadding = ButtonDefaults.TextButtonContentPadding) {
                Text("View all  →", color = Blue, fontSize = 10.sp)
            }
        }
        Row(Modifier.padding(horizontal = 18.dp).fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(Bg).padding(horizontal = 11.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("⌕", color = Muted, fontSize = 17.sp)
            Spacer(Modifier.width(8.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = TextMain, fontSize = 11.sp),
                decorationBox = { inner ->
                    if (query.isEmpty()) Text("Search processes...", color = Muted, fontSize = 11.sp)
                    inner()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth().background(PanelAlt).padding(horizontal = 18.dp, vertical = 10.dp)) {
            Text("PROCESS", color = Muted, fontSize = 9.sp, letterSpacing = 0.8.sp, modifier = Modifier.weight(2.4f))
            Text("PID", color = Muted, fontSize = 9.sp, modifier = Modifier.weight(0.7f))
            Text("CPU", color = Muted, fontSize = 9.sp, modifier = Modifier.weight(0.7f))
            Text("MEMORY", color = Muted, fontSize = 9.sp, modifier = Modifier.weight(0.9f))
            Text("STATUS", color = Muted, fontSize = 9.sp, modifier = Modifier.weight(0.8f))
        }
        val filtered = processes.filter { it.name.contains(query, ignoreCase = true) || it.pid.contains(query) }
        LazyColumn {
            items(filtered) { process -> ProcessTableRow(process) }
        }
    }
}

@Composable
private fun ProcessTableRow(process: ProcessInfo) {
    Row(
        Modifier.fillMaxWidth().border(width = 0.5.dp, color = Stroke.copy(alpha = 0.55f))
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.weight(2.4f), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(PanelAlt), contentAlignment = Alignment.Center) {
                Text(process.name.take(1), color = Blue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(9.dp))
            Text(process.name, color = TextMain, fontSize = 10.sp, maxLines = 1)
        }
        Text(process.pid, color = Muted, fontSize = 10.sp, modifier = Modifier.weight(0.7f))
        Text(process.cpu, color = if (process.cpu == "0.0%") Muted else TextMain, fontSize = 10.sp, modifier = Modifier.weight(0.7f))
        Text(process.memory, color = TextMain, fontSize = 10.sp, modifier = Modifier.weight(0.9f))
        Row(Modifier.weight(0.8f), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(process.tint))
            Spacer(Modifier.width(5.dp))
            Text(process.status, color = Muted, fontSize = 9.sp)
        }
    }
}

@Composable
private fun SystemHealthPanel(telemetry: SystemMonitor.Snapshot?, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(Panel).border(1.dp, Stroke, RoundedCornerShape(16.dp)).padding(18.dp)) {
        Text("System health", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(5.dp))
        Text("Current device snapshot", color = Muted, fontSize = 10.sp)
        Spacer(Modifier.height(20.dp))
        HealthBar("Memory pressure", telemetry?.let { "${oneDecimal(it.memoryUsedPercent)}%" } ?: "—", (telemetry?.memoryUsedPercent?.toFloat()?.div(100f) ?: 0f).coerceIn(0f, 1f), Blue)
        Spacer(Modifier.height(17.dp))
        HealthBar("CPU load", telemetry?.let { "${oneDecimal(it.cpuLoadPercent)}%" } ?: "—", (telemetry?.cpuLoadPercent?.toFloat()?.div(100f) ?: 0f).coerceIn(0f, 1f), Green)
        Spacer(Modifier.height(17.dp))
        HealthBar("Process count", telemetry?.processCount?.toString() ?: "—", 0f, Color(0xFF9A7BFF))
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFF182A27)).padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(Green))
            Spacer(Modifier.width(8.dp))
            Text(if (telemetry == null) "Waiting for telemetry" else "Live system sample received", color = if (telemetry == null) Amber else Green, fontSize = 10.sp)
        }
    }
}

@Composable
private fun HealthBar(label: String, value: String, progress: Float, color: Color) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Muted, fontSize = 10.sp)
            Text(value, color = TextMain, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(PanelAlt)) {
            Box(Modifier.fillMaxWidth(progress).fillMaxHeight().clip(CircleShape).background(color))
        }
    }
}

@Composable
private fun ActivityPanel(modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(Panel).border(1.dp, Stroke, RoundedCornerShape(16.dp)).padding(18.dp)) {
        Text("Recent activity", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(14.dp))
        ActivityLine("Process scan completed", "Just now", Green)
        Spacer(Modifier.height(13.dp))
        ActivityLine("Memory snapshot captured", "2 min ago", Blue)
        Spacer(Modifier.height(13.dp))
        ActivityLine("Diagnostics initialized", "5 min ago", Muted)
    }
}

@Composable
private fun ActivityLine(title: String, time: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(9.dp))
        Text(title, color = TextMain, fontSize = 10.sp, modifier = Modifier.weight(1f))
        Text(time, color = Muted, fontSize = 9.sp)
    }
}

@Composable
private fun PlaceholderPage(page: String) {
    Box(Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(Panel).border(1.dp, Stroke, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(page, color = TextMain, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("This module is planned for the next development milestone.", color = Muted, fontSize = 12.sp)
        }
    }
}


private fun oneDecimal(value: Double): String = String.format(Locale.US, "%.1f", value)

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1L shl 30 -> "${oneDecimal(bytes / 1_073_741_824.0)} GB"
    bytes >= 1L shl 20 -> "${oneDecimal(bytes / 1_048_576.0)} MB"
    bytes >= 1L shl 10 -> "${oneDecimal(bytes / 1_024.0)} KB"
    else -> "${bytes} B"
}
