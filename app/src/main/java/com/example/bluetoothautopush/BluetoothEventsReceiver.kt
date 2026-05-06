package com.example.bluetoothautopush

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BluetoothEventsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        when (action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                TransferScheduler.enqueueNow(context, device?.address)
            }
            BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                if (state == BluetoothDevice.BOND_BONDED) {
                    TransferScheduler.enqueueNow(context, device?.address)
                }
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                // Keep app behavior active after reboot. Work is still event-triggered.
            }
        }
    }
}
