package com.bleudev.modsync.client.util

import net.minecraft.client.Minecraft

fun Minecraft.closeScreen() = this.gui.setScreen(null)