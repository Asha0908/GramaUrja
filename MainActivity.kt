package com.example.gramaurja

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.*

// --- DATA MODELS ---
data class TransformerItem(
    val id: String,
    val name: String,
    var status: Boolean,
    var lastUpdated: String
)

data class AlertItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val time: String,
    val isWarning: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                GramaUrjaApp()
            }
        }
    }
}

@Composable
fun GramaUrjaApp() {
    var isLoggedIn by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf("Grid") }
    val alerts = remember { mutableStateListOf<AlertItem>() }
    var toastMessage by remember { mutableStateOf<AlertItem?>(null) }

    val transformers = remember {
        mutableStateListOf(
            TransformerItem("GS-001", "Primary Substation", true, "5m ago"),
            TransformerItem("GS-002", "North Sector Line", true, "45m ago"),
            TransformerItem("GS-003", "East Irrigation Hub", false, "15m ago"),
            TransformerItem("GS-004", "Community Center", true, "2h ago"),
            TransformerItem("GS-005", "Govt School Grid", true, "30m ago")
        )
    }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(3000)
            toastMessage = null
        }
    }

    if (!isLoggedIn) {
        LoginScreen { name ->
            userName = name
            isLoggedIn = true
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                bottomBar = {
                    BottomNav(activeTab) { activeTab = it }
                }
            ) { padding ->
                Column(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFFBFBFC))) {
                    AppHeader(userName)
                    Crossfade(targetState = activeTab) { tab ->
                        when (tab) {
                            "Grid" -> GridScreen(transformers, userName) { alert ->
                                alerts.add(0, alert)
                                toastMessage = alert
                            }
                            "Pump" -> PumpScreen()
                            "Logs" -> LogsScreen(alerts)
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = toastMessage != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp)
            ) {
                toastMessage?.let { msg ->
                    Surface(
                        color = if (msg.isWarning) Color(0xFFFFF8E1) else Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, if (msg.isWarning) Color(0xFFFFD54F) else Color(0xFFA5D6A7)),
                        shadowElevation = 8.dp,
                        modifier = Modifier.width(320.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (msg.isWarning) Icons.Default.Warning else Icons.Default.Check,
                                null,
                                tint = if (msg.isWarning) Color(0xFFFFA000) else Color(0xFF4CAF50)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(if (msg.isWarning) "WARNING" else "SUCCESS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                                Text(msg.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppHeader(operator: String) {
    Surface(color = Color.White, shadowElevation = 2.dp) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // FIXED: Changed FlashOn to Settings for compatibility
                Icon(Icons.Default.Settings, null, tint = Color(0xFF10B981), modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Grama Urja", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Text("POWER MONITOR", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
            Surface(color = Color(0xFFF4F4F5), shape = RoundedCornerShape(8.dp)) {
                Text("OP: ${operator.uppercase()}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun GridScreen(transformers: MutableList<TransformerItem>, operator: String, onAction: (AlertItem) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("TRANSFORMER STATUS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp)) }
        items(transformers) { item ->
            Card(
                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (item.status) Color(0xFF10B981) else Color(0xFFF1F1F4),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Settings, null, tint = if (item.status) Color.White else Color.LightGray)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("ID: ${item.id} • ${item.lastUpdated}", fontSize = 10.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = item.status,
                        onCheckedChange = { checked ->
                            item.status = checked
                            onAction(AlertItem(
                                title = if (checked) "LINE ENERGIZED" else "LINE ISOLATED",
                                message = "${item.name} toggled by $operator",
                                time = "Just now",
                                isWarning = !checked
                            ))
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981))
                    )
                }
            }
        }
    }
}

@Composable
fun PumpScreen() {
    // FIXED: Prefer mutableFloatStateOf and mutableIntStateOf
    var acres by remember { mutableFloatStateOf(1f) }
    var selectedCrop by remember { mutableStateOf("Paddy") }
    var timeLeft by remember { mutableIntStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }

    val factor = when(selectedCrop) { "Paddy" -> 4; "Sugarcane" -> 6; "Cotton" -> 3; else -> 2 }
    val totalSeconds = (acres * factor * 3600).toInt()

    LaunchedEffect(isRunning) {
        if (isRunning && timeLeft > 0) {
            while (timeLeft > 0 && isRunning) {
                delay(1000)
                timeLeft--
            }
            isRunning = false
        }
    }

    Column(Modifier.padding(24.dp)) {
        Text("PUMP SCHEDULER", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Gray)
        Spacer(Modifier.height(20.dp))

        Surface(
            color = Color(0xFF18181B),
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("REMAINING RUN TIME", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                val h = timeLeft / 3600
                val m = (timeLeft % 3600) / 60
                val s = timeLeft % 60
                val displayTime = if (isRunning) String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s) else "${(acres * factor).toInt()} HRS"

                Text(displayTime, color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        if (!isRunning) timeLeft = totalSeconds
                        isRunning = !isRunning
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isRunning) Color(0xFFEF4444) else Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isRunning) "STOP SUPPLY" else "START CYCLE", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        Text("LAND SIZE: ${acres.toInt()} ACRES", fontWeight = FontWeight.Bold)
        Slider(value = acres, onValueChange = { acres = it }, valueRange = 1f..10f, steps = 9)

        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Paddy", "Sugarcane", "Cotton", "Veg").forEach { crop ->
                Button(
                    onClick = { selectedCrop = crop },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedCrop == crop) Color(0xFFE8F5E9) else Color.White),
                    border = BorderStroke(1.dp, if (selectedCrop == crop) Color(0xFF10B981) else Color(0xFFE4E4E7)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(crop, color = if (selectedCrop == crop) Color(0xFF10B981) else Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LogsScreen(alerts: List<AlertItem>) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("OPERATIONS LOG", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp)) }
        items(alerts) { alert ->
            Card(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFF1F1F4))
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (alert.isWarning) Icons.Default.Warning else Icons.Default.Check,
                        null,
                        tint = if (alert.isWarning) Color(0xFFFFA000) else Color(0xFF10B981)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(alert.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(alert.time, fontSize = 10.sp, color = Color.Gray)
                        }
                        Text(alert.message, fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(onLogin: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(Color(0xFF18181B)).padding(40.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(color = Color(0xFF27272A), shape = RoundedCornerShape(24.dp), modifier = Modifier.size(80.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Settings, null, tint = Color(0xFF10B981), modifier = Modifier.size(40.dp)) }
        }
        Spacer(Modifier.height(24.dp))
        Text("Grama Urja", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
        Text("VILLAGE GRID AUTHORITY", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(48.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Operator Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.DarkGray, focusedBorderColor = Color(0xFF10B981), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { if (name.isNotEmpty()) onLogin(name) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("INITIALIZE AUTHORITY", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun BottomNav(active: String, onSelect: (String) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        listOf("Grid", "Pump", "Logs").forEach { tab ->
            NavigationBarItem(
                selected = active == tab,
                onClick = { onSelect(tab) },
                icon = {
                    Icon(
                        when(tab) {
                            "Grid" -> Icons.Default.Settings
                            "Pump" -> Icons.Default.DateRange
                            else -> Icons.AutoMirrored.Filled.List // FIXED: Use AutoMirrored version
                        },
                        null
                    )
                },
                label = { Text(tab, fontWeight = FontWeight.Bold) }
            )
        }
    }
}
