package com.bleudev.modsync.client.custom.gui.screens

import com.bleudev.modsync.client.ClientTempStorageManager
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.GenericWaitingScreen
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.util.ARGB
import kotlin.math.roundToInt

class ModsUpdatingScreen : GenericWaitingScreen(
    Component.translatable("modsync.update.wait.running"),
    true,
    Component.translatable("modsync.update.wait.running.wait"),
    Component.empty(),
    {},
    0,
    false,
    false
) {
    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        super.extractRenderState(graphics, mouseX, mouseY, a)
        val t = ClientTempStorageManager.getInstance()
        val tr = t.downloadReadBytes
        val tt = t.downloadTotalBytes
        if (tt != null) {
            val c = Component.literal("$tr / $tt (${(tr.toFloat() / tt * 100).roundToInt()}%)")
            graphics.centeredText(this.minecraft.font, c, this.width / 2, 142, ARGB.color(1f, TextColor.GREEN.value))
        }
    }
}