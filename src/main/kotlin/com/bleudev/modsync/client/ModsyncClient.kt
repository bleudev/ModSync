package com.bleudev.modsync.client

import com.bleudev.modsync.client.util.ModSyncer
import com.bleudev.modsync.custom.packet.payload.ModSyncInfo
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking
import net.minecraft.client.gui.screens.GenericWaitingScreen
import net.minecraft.network.chat.Component

class ModsyncClient : ClientModInitializer {
    private var updateAddress: String = ""
    private var toUpdate: List<Pair<String, String>> = listOf()

    override fun onInitializeClient() {
        ClientStorageManager.getInstance().init()
        ClientConfigurationNetworking.registerGlobalReceiver(ModSyncInfo.TYPE) { payload, _ ->
            println("GOT PACKET")
            val host = ClientTempStorageManager.getInstance().serverHost ?: return@registerGlobalReceiver
            updateAddress = "http://$host:${payload.port}"
            println("CURRENT SERVER $updateAddress")
            toUpdate = ModSyncer.getInstance().fetch(updateAddress)
            println("TO UPDATE $toUpdate")

        }
        ClientTickEvents.END_CLIENT_TICK.register { mc ->
            if (updateAddress.isNotEmpty() && toUpdate.isNotEmpty()) {
                mc.disconnect(GenericWaitingScreen.createWaitingWithoutButton(
                    Component.literal("Update mods"),
                    Component.literal("Please wait")
                ), true)
                for ((id, version) in toUpdate) {
                    ModSyncer.getInstance().sync(updateAddress, id, version)
                    println("SYNC $id $version")
                }
                toUpdate = listOf()
                updateAddress = ""
                println("FINISH")
                mc.exitWorldAndClose()
            }
        }
    }
}
