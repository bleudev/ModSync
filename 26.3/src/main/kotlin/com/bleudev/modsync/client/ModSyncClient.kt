package com.bleudev.modsync.client

import com.bleudev.modsync.client.custom.gui.screens.ConfirmUpdateScreen
import com.bleudev.modsync.client.custom.gui.screens.RestartScreen
import com.bleudev.modsync.client.util.ModSyncer
import com.bleudev.modsync.client.util.cancel
import com.bleudev.modsync.custom.packet.payload.ModSyncInfo
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.ConnectScreen

class ModSyncClient : ClientModInitializer {
    override fun onInitializeClient() {
        ClientConfigurationNetworking.registerGlobalReceiver(ModSyncInfo.TYPE) { payload, _ ->
            val temp = ClientTempStorageManager.getInstance()
            val host = temp.serverHost ?: return@registerGlobalReceiver
            @Suppress("HttpUrlsUsage")
            temp.updateAddress = "http://$host:${payload.port}"
            temp.updateData = ModSyncer.getInstance().fetch(temp.updateAddress)
        }
        ClientTickEvents.END_CLIENT_TICK.register { mc ->
            val temp = ClientTempStorageManager.getInstance()
            val already = temp.serverAddress in temp.requireRestartAddresses
            if (!temp.updating) {
                if (temp.updateAddress.isNotEmpty() && temp.updateData.isNotEmpty() && !already) {
                    temp.updating = true
                    mc.ensureCancelConnect()
                    mc.disconnect(ConfirmUpdateScreen.createDefault(), true)
                }
                if (mc.gui.screen() is ConnectScreen && already) {
                    mc.ensureCancelConnect()
                    RestartScreen.show()
                }
            }
        }
    }

    private fun Minecraft.ensureCancelConnect() {
        (this.gui.screen() as? ConnectScreen)?.cancel()
    }
}
