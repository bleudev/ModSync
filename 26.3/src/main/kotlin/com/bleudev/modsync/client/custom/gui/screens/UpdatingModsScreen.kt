package com.bleudev.modsync.client.custom.gui.screens

import com.bleudev.modsync.ModSyncHttpServer
import com.bleudev.modsync.client.ClientTempStorageManager
import com.bleudev.modsync.client.util.MessageHelper
import com.bleudev.modsync.client.util.ModSyncer
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.GenericWaitingScreen
import net.minecraft.client.gui.screens.TitleScreen
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
import net.minecraft.network.chat.Component

class UpdatingModsScreen(val modMetadata: ModSyncHttpServer.ModSyncMetadata.ModMetadata, val onFinish: () -> Unit) : GenericWaitingScreen(
    Component.translatable("modsync.update.updating.title"),
    true,
    generateMessage(modMetadata),
    Component.translatable("gui.cancel"),
    {},
    0,
    true,
    false
) {
    private var syncThread: Thread? = null

    override fun added() {
        super.added()
        syncThread = Thread {
            val temp = ClientTempStorageManager.getInstance()
            ModSyncer.getInstance().sync(temp.updateAddress, modMetadata)
            temp.updateData = listOf()
            temp.updateAddress = ""
            Minecraft.getInstance().execute { onFinish() }
            temp.updating = false
        }
        syncThread!!.start()
    }
    override fun onClose() {
        super.onClose()
        syncThread?.join()
        Minecraft.getInstance().gui.setScreen(JoinMultiplayerScreen(TitleScreen()))
    }

    companion object {
        private fun generateMessage(modMetadata: ModSyncHttpServer.ModSyncMetadata.ModMetadata): Component =
            Component.translatable("modsync.update.updating.message.top")
                .append(Component
                    .literal("${modMetadata.id}\n")
                    .withStyle(ChatFormatting.YELLOW)
                )
                .append(MessageHelper.getVersionChangeMessage(modMetadata))
                .append("\n\n")
                .append(Component.translatable("modsync.update.updating.message.wait"))

        fun startUpdating(updateData: List<ModSyncHttpServer.ModSyncMetadata.ModMetadata>) {
            if (updateData.isNotEmpty()) {
                val screen = UpdatingModsScreen(updateData[0]) {
                    startUpdating(updateData.subList(1, updateData.size))
                }
                Minecraft.getInstance().gui.setScreen(screen)
            } else {
                RestartScreen.show()
            }
        }
    }
}