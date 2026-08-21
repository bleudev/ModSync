package com.bleudev.modsync.client

import com.bleudev.modsync.client.util.ModSyncer
import com.bleudev.modsync.custom.packet.payload.ModSyncInfo
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.ConfirmScreen
import net.minecraft.network.chat.Component

class ModsyncClient : ClientModInitializer {
    override fun onInitializeClient() {
        ClientStorageManager.getInstance().init()
        ClientPlayNetworking.registerGlobalReceiver(ModSyncInfo.TYPE) { payload, ctx ->
            val split = ctx.client().currentServer?.ip?.split(":") ?: return@registerGlobalReceiver
            val address = split.subList(0, split.size-1).joinToString(":")

            val p = ClientStorageManager.getInstance().load()?.servers[address]?.port ?: -1
            if (p != payload.port) {
                val s = ClientStorageManager.getInstance().load() ?: return@registerGlobalReceiver
                s.modify(address) {
                    it.port = payload.port
                    it
                }
                s.save()
            }
            if (ModSyncer.getInstance().trySync(address)) {
                ctx.responseSender().disconnect(Component.literal("Updating mods."))
                Minecraft.getInstance().gui.setScreen(ConfirmScreen(
                    { if (it) Minecraft.getInstance().close() },
                    Component.literal("Finished"),
                    Component.literal("Restart the game?"),
                    Component.literal("Yes"),
                    Component.literal("No"),
                ))
            }
        }
    }
}
