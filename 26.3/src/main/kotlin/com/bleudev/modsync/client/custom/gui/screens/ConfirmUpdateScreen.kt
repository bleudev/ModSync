package com.bleudev.modsync.client.custom.gui.screens

import com.bleudev.modsync.client.ClientTempStorageManager
import com.bleudev.modsync.client.util.MessageHelper
import com.bleudev.modsync.client.util.closeScreen
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.ConfirmScreen
import net.minecraft.network.chat.Component

class ConfirmUpdateScreen(val autoConfirm: Boolean = false) : ConfirmScreen(
    ::execute,
    Component.translatable("modsync.update.confirm.title"),
    getMessage(),
    Component.translatable("modsync.update.confirm.yes"),
    Component.translatable("modsync.update.confirm.no")
) {
    override fun tick() {
        if (autoConfirm) {
            callback.accept(true)
            return
        }
        super.tick()
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        if (autoConfirm) {
            return
        }
        super.extractRenderState(graphics, mouseX, mouseY, a)
    }


    companion object {
        private fun getMessage(): Component {
            val message = Component.translatable("modsync.update.confirm.message")
            for (data in ClientTempStorageManager.getInstance().updateData) {
                message.append("\n").append(MessageHelper.getVersionChangeMessage(data, true))
            }
            return message
        }

        private fun execute(confirmed: Boolean) {
            val mc = Minecraft.getInstance()
            val temp = ClientTempStorageManager.getInstance()
            if (confirmed) {
                val already = temp.serverAddress in temp.requireRestartAddresses
                if (temp.updateAddress.isNotEmpty() && temp.updateData.isNotEmpty() && !already) {
                    temp.serverAddress?.let { a -> temp.requireRestartAddresses.add(a) }
                    UpdatingModsScreen.startUpdating(temp.updateData)
                } else {
                    mc.closeScreen()
                }
            } else {
                temp.updateData = listOf()
                temp.updateAddress = ""
                temp.updating = false
                mc.closeScreen()
            }
        }

        fun createDefault(): ConfirmUpdateScreen = ConfirmUpdateScreen(false)
    }
}