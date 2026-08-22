package com.bleudev.modsync.client.util

import com.bleudev.modsync.i.mixin.client.IConnectScreen
import net.minecraft.client.gui.screens.ConnectScreen

fun ConnectScreen.cancel() = (this as IConnectScreen).`modsync$cancel`()