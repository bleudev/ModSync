package com.bleudev.modsync.client.custom.gui.screens

import com.bleudev.modsync.ModSyncHttpServer
import com.bleudev.modsync.client.ClientTempStorageManager
import com.bleudev.modsync.client.util.MessageHelper
import com.bleudev.modsync.client.util.ModSyncer
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.GenericWaitingScreen
import net.minecraft.client.gui.screens.TitleScreen
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
import net.minecraft.network.chat.Component
import kotlin.math.roundToInt

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
    private val progressAtomic: AtomicRef<Float> = atomic(0f)

    override fun added() {
        super.added()
        syncThread = Thread {
            val temp = ClientTempStorageManager.getInstance()
            ModSyncer.getInstance().sync(temp.updateAddress, modMetadata, progressAtomic)
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

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        super.extractRenderState(graphics, mouseX, mouseY, a)

        this.drawProgressBar(graphics, width / 2 - 100, height / 2, 200, 2, progressAtomic.value)
    }

    private fun drawProgressBar(
        graphics: GuiGraphicsExtractor,
        left: Int,
        top: Int,
        width: Int,
        height: Int,
        progress: Float
    ) {
        graphics.fill(left, top, left + width, top + height, -16777216)
        graphics.fill(left, top, left + (progress * width.toFloat()).roundToInt(), top + height, -16711936)
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