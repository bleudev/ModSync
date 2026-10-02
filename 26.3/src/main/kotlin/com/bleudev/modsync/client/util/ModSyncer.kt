package com.bleudev.modsync.client.util

import com.bleudev.modsync.http.clientbound.ClientHttpHelper
import com.bleudev.modsync.http.serverbound.ModSyncHttpServer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.api.Version
import net.fabricmc.loader.api.metadata.ModMetadata
import java.nio.file.Files
import java.nio.file.Path
import kotlin.jvm.optionals.getOrNull

class ModSyncer private constructor(private val modsDir: Path) {
    fun fetch(address: String): List<ModSyncHttpServer.ModSyncMetadata.ModMetadata> {
        val h = ClientHttpHelper(address)
        val mods = h.metadata().mods
        val ans = arrayListOf<ModSyncHttpServer.ModSyncMetadata.ModMetadata>()
        for (data in mods) {
            val current = FabricLoader.getInstance().getModContainer(data.id)
                .map(ModContainer::getMetadata)
                .map(ModMetadata::getVersion)
                .getOrNull()
            if (current == null || current < Version.parse(data.version)) {
                ans.add(data)
            }
        }
        return ans.toList()
    }

    fun sync(address: String, data: ModSyncHttpServer.ModSyncMetadata.ModMetadata): Boolean {
        val h = ClientHttpHelper(address)
        FabricLoader.getInstance().getModContainer(data.id).ifPresent {
            for (path in it.origin.paths) {
                Files.deleteIfExists(path)
            }
        }
        h.mod(modsDir, data)
        return true
    }

    companion object {
        fun getInstance(): ModSyncer = ModSyncer(FabricLoader.getInstance().gameDir.resolve("mods"))
    }
}