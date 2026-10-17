package com.bleudev.modsync.client.util

import com.bleudev.modsync.client.ClientTempStorageManager
import com.bleudev.modsync.http.clientbound.ClientHttpHelper
import com.bleudev.modsync.http.serverbound.serialization.ModSyncMetadata
import kotlinx.io.IOException
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.api.Version
import net.fabricmc.loader.api.metadata.ModMetadata
import java.net.http.HttpConnectTimeoutException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.jvm.optionals.getOrNull

class ModSyncer private constructor(private val modsDir: Path) {
    fun fetch(address: String): List<ModSyncMetadata.ModMetadata> {
        val h = ClientHttpHelper(address)
        try {
            val mods = h.metadata().mods
            val ans = arrayListOf<ModSyncMetadata.ModMetadata>()
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
        } catch (_: HttpConnectTimeoutException) {
            return listOf()
        }
    }

    fun sync(address: String, data: ModSyncMetadata.ModMetadata): Boolean {
        val h = ClientHttpHelper(address)
        try {
            h.mod(modsDir, data) { downloaded, total ->
                ClientTempStorageManager.getInstance().downloadReadBytes = downloaded
                ClientTempStorageManager.getInstance().downloadTotalBytes = total
            }
        } catch (_: HttpConnectTimeoutException) {
            return false
        }
        try {
            FabricLoader.getInstance().getModContainer(data.id).ifPresent {
                for (path in it.origin.paths) {
                    Files.deleteIfExists(path)
                }
            }
        } catch (_: IOException) { // Some error with deleting
            return true
        }
        return true
    }

    companion object {
        fun getInstance(): ModSyncer = ModSyncer(FabricLoader.getInstance().gameDir.resolve("mods"))
    }
}