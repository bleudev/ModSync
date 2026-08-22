package com.bleudev.modsync.client

import com.bleudev.modsync.client.util.ModSyncer
import com.bleudev.modsync.custom.packet.payload.ModSyncInfo
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.ConfirmScreen
import net.minecraft.client.gui.screens.ConnectScreen
import net.minecraft.client.gui.screens.GenericWaitingScreen
import net.minecraft.network.chat.Component

class ModSyncClient : ClientModInitializer {
    private val requireRestartAddresses = arrayListOf<String>()

    private var updateAddress: String = ""
    private var toUpdate: List<Pair<String, String>> = listOf()
    private var updating: Boolean = false
    private var shouldShowRestartScreen: Boolean = false

    override fun onInitializeClient() {
        ClientConfigurationNetworking.registerGlobalReceiver(ModSyncInfo.TYPE) { payload, _ ->
            val host = ClientTempStorageManager.getInstance().serverHost ?: return@registerGlobalReceiver
            updateAddress = "http://$host:${payload.port}"
            toUpdate = ModSyncer.getInstance().fetch(updateAddress)
        }
        ClientTickEvents.END_CLIENT_TICK.register { mc ->
            if (shouldShowRestartScreen) {
                shouldShowRestartScreen = false
                mc.showRestartScreen()
            }

            val already = ClientTempStorageManager.getInstance().serverAddress in requireRestartAddresses
            if (!updating) {
                if (updateAddress.isNotEmpty() && toUpdate.isNotEmpty() && !already) {
                    updating = true
                    mc.disconnect(approveUpdateScreen(), true)
                }
                if (mc.gui.screen() is ConnectScreen && already) {
                    mc.showRestartScreen()
                }
            }
        }
    }

    private fun approveUpdateScreen(): ConfirmScreen {
        val message = Component.translatable("modsync.update.approve.message")
        for ((id, version) in toUpdate) {
            val container = FabricLoader.getInstance().getModContainer(id)

            if (container.isPresent) {
                val current = container.get().metadata.version.friendlyString
                message.append(Component.literal("\n$id: $current -> $version").withStyle(ChatFormatting.GREEN))
            } else {
                message.append(Component.literal("\n$id: $version").withStyle(ChatFormatting.AQUA))
            }
        }
        return ConfirmScreen(
            {
                if (it) {
                    val already = ClientTempStorageManager.getInstance().serverAddress in requireRestartAddresses
                    if (updateAddress.isNotEmpty() && toUpdate.isNotEmpty() && !already) {
                        ClientTempStorageManager.getInstance().serverAddress?.let { a -> requireRestartAddresses.add(a) }
                        Minecraft.getInstance().gui.setScreen(updatingScreen())
                        Thread {
                            for ((id, version) in toUpdate) {
                                ModSyncer.getInstance().sync(updateAddress, id, version)
                            }
                            toUpdate = listOf()
                            updateAddress = ""
                            shouldShowRestartScreen = true
                            updating = false
                        }.start()
                    } else {
                        Minecraft.getInstance().gui.setScreen(null)
                    }
                } else {
                    toUpdate = listOf()
                    updateAddress = ""
                    updating = false
                    Minecraft.getInstance().gui.setScreen(null)
                }
            },
            Component.translatable("modsync.update.approve.title"),
            message,
            Component.translatable("modsync.update.approve.yes"),
            Component.translatable("modsync.update.approve.no")
        )
    }

    private fun updatingScreen(): GenericWaitingScreen = GenericWaitingScreen.createWaitingWithoutButton(
        Component.translatable("modsync.update.wait.running"),
        Component.translatable("modsync.update.wait.running.wait")
    )

    private fun Minecraft.showRestartScreen() {
        this.setScreenAndShow(ConfirmScreen(
            {
                if (it) {
                    this.close()
                } else {
                    this.gui.setScreen(null)
                }
            },
            Component.translatable("modsync.update.end.restart"),
            Component.translatable("modsync.update.end.restart.more"),
            Component.translatable("modsync.update.end.restart.yes"),
            Component.translatable("modsync.update.end.restart.no")
        ))
    }
}
