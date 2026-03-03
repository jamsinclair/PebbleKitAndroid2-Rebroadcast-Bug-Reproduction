package com.github.jamsinclair.pebblekittest

import android.content.Intent
import android.util.Log
import io.rebble.pebblekit2.client.BasePebbleListenerService
import io.rebble.pebblekit2.client.DefaultPebbleSender
import io.rebble.pebblekit2.common.model.PebbleDictionary
import io.rebble.pebblekit2.common.model.PebbleDictionaryItem
import io.rebble.pebblekit2.common.model.WatchIdentifier
import io.rebble.pebblekit2.common.model.ReceiveResult
import java.util.UUID

class PebbleListenerService : BasePebbleListenerService() {
    companion object {
        private const val TAG = "PebbleListenerService"
        private const val KEY_PING = 0
        private const val KEY_PONG = 1
        const val ACTION_PEBBLE_EVENT = "com.github.jamsinclair.pebblekittest.PEBBLE_EVENT"
        const val EXTRA_MESSAGE = "message"
    }

    override suspend fun onMessageReceived(
        watchappUUID: UUID,
        data: PebbleDictionary,
        watch: WatchIdentifier
    ): ReceiveResult {
        Log.d(TAG, "Message received from watch: UUID=$watchappUUID, keys=${data.keys}")

        // Check if this is a ping message
        val pingItem = data[KEY_PING.toUInt()]
        if (pingItem != null) {
            Log.d(TAG, "Ping detected on key $KEY_PING")
            sendEventBroadcast("Ping received from watch")

            // Send pong back using the watchappUUID from the callback
            try {
                val sender = DefaultPebbleSender(baseContext)
                val dictionary: Map<UInt, PebbleDictionaryItem> = mapOf(
                    KEY_PONG.toUInt() to PebbleDictionaryItem.UInt8(1u)
                )
                sender.sendDataToPebble(watchappUUID, dictionary)
                Log.d(TAG, "Pong sent to watch on key $KEY_PONG")
                sendEventBroadcast("Pong sent to watch")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending pong", e)
                sendEventBroadcast("Error sending pong: ${e.message}")
            }
        } else {
            Log.d(TAG, "Message received but no PING key ($KEY_PING) found")
            sendEventBroadcast("Message received without PING key")
        }

        return ReceiveResult.Ack
    }

    override fun onAppOpened(watchappUUID: UUID, watch: WatchIdentifier) {
        Log.d(TAG, "Pebble app opened: UUID=$watchappUUID, watch=$watch")
        sendEventBroadcast("Pebble app opened")
    }

    override fun onAppClosed(watchappUUID: UUID, watch: WatchIdentifier) {
        Log.d(TAG, "Pebble app closed: UUID=$watchappUUID")
        sendEventBroadcast("Pebble app closed")
    }

    private fun sendEventBroadcast(message: String) {
        val intent = Intent(ACTION_PEBBLE_EVENT).apply {
            putExtra(EXTRA_MESSAGE, message)
            setPackage(baseContext.packageName)
        }
        sendBroadcast(intent)
    }
}
