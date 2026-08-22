package com.bleudev.modsync.client

import com.bleudev.modsync.client.util.ModSyncer
import com.bleudev.modsync.custom.packet.payload.ModSyncInfo
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.ConfirmScreen
import net.minecraft.client.gui.screens.ConnectScreen
import net.minecraft.client.gui.screens.GenericWaitingScreen
import net.minecraft.network.chat.Component

class ModsyncClient : ClientModInitializer {
    private val requireRestartAddresses = arrayListOf<String>()

    private var updateAddress: String = ""
    private var toUpdate: List<Pair<String, String>> = listOf()
    private var updating: Boolean = false

    override fun onInitializeClient() {
        ClientConfigurationNetworking.registerGlobalReceiver(ModSyncInfo.TYPE) { payload, _ ->
            println("GOT PACKET")
            val host = ClientTempStorageManager.getInstance().serverHost ?: return@registerGlobalReceiver
            updateAddress = "http://$host:${payload.port}"
            println("CURRENT SERVER $updateAddress")
            toUpdate = ModSyncer.getInstance().fetch(updateAddress)
            println("TO UPDATE $toUpdate")

        }
        ClientTickEvents.END_CLIENT_TICK.register { mc ->
            val already = ClientTempStorageManager.getInstance().serverAddress in requireRestartAddresses
            if (!updating) {
                if (updateAddress.isNotEmpty() && toUpdate.isNotEmpty() && !already) {
                    updating = true
                    ClientTempStorageManager.getInstance().serverAddress?.let { requireRestartAddresses.add(it) }
                    mc.disconnect(GenericWaitingScreen.createWaitingWithoutButton(
                        Component.literal("Update mods"),
                        Component.literal("Please wait")
                    ), true)
                    mc.execute {
                        for ((id, version) in toUpdate) {
                            ModSyncer.getInstance().sync(updateAddress, id, version)
                            println("SYNC $id $version")
                        }
                        toUpdate = listOf()
                        updateAddress = ""
                        println("FINISH $requireRestartAddresses")
                        mc.showRestartScreen()
                        updating = false
                    }
                }
                if (mc.gui.screen() is ConnectScreen && already) {
                    println("NEED TO RESTART")
                    mc.showRestartScreen()
                }
            }
        }
    }

    private fun Minecraft.showRestartScreen() {
        this.setScreenAndShow(ConfirmScreen(
            {
                if (it) {
                    this.close()
                } else {
                    this.gui.setScreen(null)
                }
            },
            Component.literal("Restart Minecraft"),
            Component.literal("To connect to this server you need to restart the game")
        ))
    }
}
