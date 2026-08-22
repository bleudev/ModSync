package com.bleudev.modsync.client.util

import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.api.Version
import net.fabricmc.loader.api.metadata.ModMetadata
import java.nio.file.Files
import java.nio.file.Path
import kotlin.jvm.optionals.getOrNull

class ModSyncer private constructor(private val modsDir: Path) {
    fun fetch(address: String): List<Pair<String, String>> {
        val h = ClientHttpHelper(address)
        val mods = h.metadata().mods
        val ans = arrayListOf<Pair<String, String>>()
        for ((id, version) in mods) {
            val current = FabricLoader.getInstance().getModContainer(id)
                .map(ModContainer::getMetadata)
                .map(ModMetadata::getVersion)
                .getOrNull()
            if (current == null || current < Version.parse(version)) {
                ans.add(id to version)
            }
        }
        return ans.toList()
    }

    fun sync(address: String, id: String, version: String): Boolean {
        val h = ClientHttpHelper(address)
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