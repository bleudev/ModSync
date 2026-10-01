package com.bleudev.modsync.platform

import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path

object ModSyncPlatform {
    val configDir: Path get() = FabricLoader.getInstance().configDir
}