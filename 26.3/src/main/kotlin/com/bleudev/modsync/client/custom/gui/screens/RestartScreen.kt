package com.bleudev.modsync.client.custom.gui.screens

import com.bleudev.modsync.client.util.closeScreen
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.ConfirmScreen
import net.minecraft.network.chat.Component

@Environment(EnvType.CLIENT)
class RestartScreen(val autoRestart: Boolean = false) : ConfirmScreen(
    {
        if (it) {
            Minecraft.getInstance().close()
        } else {
            Minecraft.getInstance().closeScreen()
        }
    },
    Component.translatable("modsync.update.end.restart"),
    Component.translatable("modsync.update.end.restart.more"),
    Component.translatable("modsync.update.end.restart.yes"),
    Component.translatable("modsync.update.end.restart.no")
) {
    override fun tick() {
        if (autoRestart) {
            callback.accept(true)
            return
        }
        super.tick()
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        if (autoRestart) {
            return
        }
        super.extractRenderState(graphics, mouseX, mouseY, a)
    }

    companion object {
        fun show() {
            Minecraft.getInstance().setScreenAndShow(RestartScreen())
        }
    }
}