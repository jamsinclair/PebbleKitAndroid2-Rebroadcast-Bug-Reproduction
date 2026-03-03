package com.github.jamsinclair.pebblekittest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jamsinclair.pebblekittest.ui.theme.PebbleKitTestTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    private val logEvents = mutableListOf<String>()
    private var pebbleReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        addLog("App started")
        renderUI()

        // Register broadcast receiver for Pebble events
        pebbleReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val message = intent.getStringExtra(PebbleListenerService.EXTRA_MESSAGE) ?: "Unknown event"
                addLog(message)
                renderUI()
            }
        }

        val filter = IntentFilter(PebbleListenerService.ACTION_PEBBLE_EVENT)
        registerReceiver(pebbleReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
    }

    override fun onDestroy() {
        super.onDestroy()
        pebbleReceiver?.let { unregisterReceiver(it) }
    }

    private fun addLog(message: String) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        logEvents.add(0, "[$timestamp] $message")
    }

    private fun renderUI() {
        setContent {
            PebbleKitTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LogScreen(
                        events = logEvents.toList(),
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun LogScreen(events: List<String>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Pebble Ping/Pong Log",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(events) { event ->
                Text(
                    text = event,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LogScreenPreview() {
    PebbleKitTestTheme {
        LogScreen(
            events = listOf(
                "[10:30:45] App started",
                "[10:30:46] Pong sent to watch",
                "[10:30:47] Pong sent to watch"
            )
        )
    }
}
