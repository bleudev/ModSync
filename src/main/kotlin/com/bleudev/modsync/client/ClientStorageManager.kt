package com.bleudev.modsync.client

import com.bleudev.modsync.LOGGER
import com.bleudev.modsync.ModSync.Companion.JSON
import kotlinx.serialization.Serializable
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.Path

class ClientStorageManager private constructor(private val storagePath: Path) {
    fun load(): ClientStorageData? {
        try {
            return JSON.decodeFromString<ClientStorageData>(Files.readString(storagePath))
        }
        catch (e: Throwable) {
            LOGGER.error("Error while loading config:\n$e\n\nPlease report about it.")
        }
        return null
    }

    fun save(data: ClientStorageData) {
        try {
            Files.writeString(storagePath, JSON.encodeToString(data))
        }
        catch (e: Throwable) {
            LOGGER.error("Error while saving config:\n$e\n\nPlease report about it.")
        }
    }

    fun init() {
        try {
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath.parent)
                Files.writeString(storagePath, "{}")
            }
        } catch (e: Throwable) {
            LOGGER.error("Error while initializing config:\n$e\n\nPlease report about it.")
        }
        save(load() ?: return)
    }

    companion object {
        fun getInstance() = ClientStorageManager(
            FabricLoader.getInstance().configDir.resolve("modsync").resolve("client.storage.json")
        )
    }

    @Serializable
    data class ClientStorageData(val servers: HashMap<String, ServerData> = hashMapOf()) {
        @Serializable
        data class ServerData(var port: Int)
        fun modify(address: String, modifyFunction: (ServerData) -> ServerData) {
            servers[address] = modifyFunction(servers[address] ?: ServerData(-1))
        }
        fun save() {
            getInstance().save(this)
        }
    }
}