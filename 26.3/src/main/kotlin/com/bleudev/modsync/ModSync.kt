package com.bleudev.modsync

import com.bleudev.modsync.custom.ModSyncPackets
import kotlinx.serialization.json.Json
import net.fabricmc.api.ModInitializer
import net.minecraft.resources.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory


val LOGGER_GENERAL: Logger = LoggerFactory.getLogger("ModSync")
val LOGGER_DISCOVER: Logger = LoggerFactory.getLogger("ModSync/discover")
val LOGGER_REQUEST: Logger = LoggerFactory.getLogger("ModSync/request")
const val MOD_ID = "modsync"
fun resolveIdentifier(path: String) : Identifier = Identifier.fromNamespaceAndPath(MOD_ID, path)

class ModSync : ModInitializer {
    companion object {
        internal val JSON: Json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }
    }

    override fun onInitialize() {
        ModSyncPackets.initialize()
    }
}
