package com.bleudev.modsync.client.util

import com.bleudev.modsync.client.ClientStorageManager
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.api.Version
import net.fabricmc.loader.api.metadata.ModMetadata
import java.nio.file.Files
import java.nio.file.Path
import kotlin.jvm.optionals.getOrNull

class ModSyncer private constructor(private val modsDir: Path) {
    private fun httpHelper(address: String): ClientHttpHelper? =
        ClientHttpHelper("$address:${ClientStorageManager.getInstance().load()?.servers[address]?.port ?: return null}")

    fun trySync(address: String): Boolean {
        val h = httpHelper(address) ?: return false
        val mods = h.metadata().mods
        var bl = false
        for ((id, version) in mods) {
            val current = FabricLoader.getInstance().getModContainer(id)
                .map(ModContainer::getMetadata)
                .map(ModMetadata::getVersion)
                .getOrNull()
            if (current == null || current < Version.parse(version)) {
                sync(address, id, version)
                bl = true
            }
        }
        return bl
    }

    private fun sync(address: String, id: String, version: String): Boolean {
        val h = httpHelper(address) ?: return false
        FabricLoader.getInstance().getModContainer(id).ifPresent {
            for (path in it.origin.paths) {
                Files.deleteIfExists(path)
            }
        }
        h.mod(modsDir, id, version)
        return true
    }

    companion object {
        fun getInstance(): ModSyncer = ModSyncer(FabricLoader.getInstance().gameDir.resolve("mods"))
    }
}